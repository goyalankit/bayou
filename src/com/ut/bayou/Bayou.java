package com.ut.bayou;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Scanner;


public class Bayou {



    private static Logger logger = Logger.getLogger("Bayou");
    private static Scanner scanner;
    private static int nextPort;
    private static HashMap<Integer, Client> clients = new HashMap<Integer, Client>();
    private static HashMap<Integer, Server> servers = new HashMap<Integer, Server>();
    private static HashMap<Integer, Integer> serverport = new HashMap<Integer, Integer>();

    public static void main(String[] args){
        nextPort = 1561;
        logger.info("$ Type help to get list of commands");
        scanner = new Scanner(System.in);
        init();
    }

    public static void init(){
        while (true){
            if(scanner.hasNext()){
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

                switch (c){
                    case PING:
                        logger.info("PONG");
                        break;
                    case START:
                        if(s[1].toUpperCase().equals(Constants.CLIENT))
                            startClient(Integer.parseInt(s[2]), Integer.parseInt(s[3]));
                        else if(s[1].toUpperCase().equals(Constants.SERVER))
                            startServer(Integer.parseInt(s[2]));
                        break;
                    case EXIT:
                        logger.info("Exiting Bayou");
                        System.exit(0);
                        break;
                    case INVALID:
                        logger.error("Invalid Command");
                        break;
                    case HELP:
                        logger.info(c.help());
                        break;
                    default:
                        logger.error("Unknown Command");
                        break;
                }
            }
        }
    }

    public static void startClient(int clNum, int svrNum){
        Client client = null;
        Server server = servers.get(svrNum);
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
            servers.put(svrNum, server);

        }else{
            logger.error("Server number "+ svrNum + " already running");
        }


    }
}


