package com.ut.bayou;

import org.apache.log4j.Logger;

import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: ankit
 * Date: 11/21/13
 * Time: 10:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class Bayou {



    private static Logger logger = Logger.getLogger("Bayou");
    private static Scanner scanner;

    public static void main(String[] args){
        logger.info("Initiating Bayou...");
        logger.info("Type help to get list of commands");
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
                        logger.debug("Starting Bayou");
                        break;
                    case EXIT:
                        logger.debug("Exiting Bayou");
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


