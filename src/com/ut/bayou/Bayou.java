package com.ut.bayou;

import org.apache.log4j.Logger;

import java.util.Scanner;


public class Bayou {



    private static Logger logger = Logger.getLogger("Bayou");
    private static Scanner scanner;

    public static void main(String[] args){
        Server server = new Server(1, 1561);
        logger.info("$ Type help to get list of commands");
        scanner = new Scanner(System.in);
        init();
    }

    public static void init(){
        while (true){
            if(scanner.hasNext()){
                String input = scanner.nextLine();
                Commands c = null;

                try{
                    c = Commands.valueOf(input.toUpperCase());
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
                        Client client = new Client(1, 1561);
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

}


