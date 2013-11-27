package com.ut.bayou;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;


public class Bayou {

    private static Logger logger = Logger.getLogger("Bayou");
    private static Scanner scanner;
    private static int nextPort;
    private static HashMap<Integer, Client> clients = new HashMap<Integer, Client>();
    private static HashMap<ServerId, Server> servers = new HashMap<ServerId, Server>();
    private static HashMap<Integer, Integer> serverport = new HashMap<Integer, Integer>();
    private static HashMap<Integer, ServerId> easyServers = new HashMap<Integer, ServerId>();
    private static boolean runScript = false;
    private static String scriptName;
    private static long delayInterval;
    private static int primaryServer = -1;


    public static void main(String[] args){
        nextPort = 1561;
        delayInterval = 0;
        logger.info("$ Type help to get list of commands");
        initializeRun(); //Method to set all the configurations for a particular run.
        if(runScript)
            try {
                scanner = new Scanner(new File("src/scripts/"+scriptName));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        else
            scanner = new Scanner(System.in);


        init();
    }

    public static void initializeRun(){
        //Properties prop = new Properties();
        Properties prop = null;
        try {
            Properties defaultProps = new Properties();
            logger.info("$ Loading properties from default.properties");
            FileInputStream in = new FileInputStream("src/default.properties");
            defaultProps.load(in);
            in.close();

            prop  = new Properties(defaultProps);

            logger.info(" -> Overriding properties from bayou.properties");
            in = new FileInputStream("src/bayou.properties");
            prop.load(in);
            in.close();

            logger.debug(prop.getProperty("scriptName"));

        } catch (IOException e) {
            e.printStackTrace();
        }

        runScript = Boolean.parseBoolean(prop.getProperty("runScript"));
        scriptName = prop.getProperty("scriptName");
        delayInterval = Long.parseLong(prop.getProperty("delayInterval"));

    }

    public static void init(){
        while (true){
            if(scanner.hasNext()){

                try {
                    Thread.sleep(delayInterval); //delay the execution
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                String input = scanner.nextLine();
                Commands c = null;
                String[] s = input.split(" ");
                try{
                    c = Commands.valueOf(s[0].toUpperCase());
                }catch (IllegalArgumentException e){
                    if(input.equals("")) {
                        continue;         }
                    else
                        c = Commands.INVALID;
                }
                executeCommand(c, s);
            }
        }
    }

    private static void executeCommand(Commands c, String[] s) {
        try {

            if(runScript)
                System.out.println("COMMAND: "+c);

            switch (c) {
                case PING:
                    logger.info("PONG");
                    break;
                case START:
                    if (s[1].toUpperCase().equals(Constants.CLIENT))
                        startClient(Integer.parseInt(s[2]), Integer.parseInt(s[3]));
                    break;
                case JOIN:
                    startServer(Integer.parseInt(s[1]));
                    break;
                case RECONNECT:
                    reconnectServer(Integer.parseInt(s[1]));
                    break;
                case PLAYLIST:
                    clients.get(Integer.parseInt(s[1])).executeUserCommand(s);
                    break;
                case PRINTLOG:
                    if(s.length > 1)
                        servers.get(easyServers.get(Integer.parseInt(s[1]))).printLog();
                    else
                        for(Server server: servers.values())
                            server.printLog();
                    break;
                case PRINTSID:
                    servers.get(easyServers.get(Integer.parseInt(s[1]))).printServerId();
                    break;
                case EXIT:
                    logger.info("Exiting Bayou");
                    System.exit(0);
                    break;
                case STARTENTROPY:
                    servers.get(easyServers.get(Integer.parseInt(s[1]))).startEntropyWith(easyServers.get(Integer.parseInt(s[2])));
                    break;
                case PRINTSERVERPLAYLIST:
                    servers.get(easyServers.get(Integer.parseInt(s[1]))).printServerPlaylist();
                    break;
                case HELP:
                    logger.info(c.help());
                    break;

                case INVALID:
                default:
                    logger.error("Unknown Command");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Invalid Command");
        }
    }

    public static void reconnectServer(int svrNum){
        for(ServerId sId : servers.keySet()){
            if(sId.hrNumber != svrNum){
                servers.get(sId).connectToYou(easyServers.get(svrNum), serverport.get(svrNum));
            }
        }
    }

    public static void startClient(int clNum, int svrNum){
        Client client = null;
        Server server = servers.get(easyServers.get(svrNum));
        if(!clients.containsKey(clNum) && server!=null){
            client = new Client(clNum, serverport.get(svrNum));
            clients.put(clNum, client);
        }else{
            logger.error("Client already running");
        }
    }

    public static void startServer(int svrNum){
        Server server = null;
        if(!servers.containsKey(svrNum)){
            serverport.put(svrNum, nextPort++);
            server = new Server(svrNum, serverport.get(svrNum));
            if(primaryServer == -1){
                server.setPrimary(true);
                primaryServer = svrNum;
                server.setServerId(new ServerId(System.currentTimeMillis(), null, 0));
                easyServers.put(svrNum, server.getServerId());
            }else{
                Write creationWrite = servers.get(easyServers.get(0)).addCreationWrite(svrNum);
                server.updateServerIdentity(creationWrite, svrNum);
            }
            servers.put(server.getServerId(), server);
            easyServers.put(svrNum, server.getServerId());

            if(primaryServer != svrNum){
                servers.get(easyServers.get(0)).connectToYou(easyServers.get(svrNum), serverport.get(svrNum));
                //reconnectServer(svrNum);
                servers.get(easyServers.get(0)).startEntropyWith(server.getServerId());
            }

        }else{
            logger.error("Server number " + svrNum + " already running");
        }
    }

}


