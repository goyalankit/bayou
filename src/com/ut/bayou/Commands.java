package com.ut.bayou;

public enum Commands {


    START("Start Bayou"), EXIT("exit this system"), INVALID("Invalid Command"), PING("Status Check"), HELP("Bring up this menu");

    private String description;

    Commands(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    String help(){
        String help = "";
        for(Commands c :Commands.values()){
            help +=  "\n" + c.toString() + ": " +c.getDescription();
        }
      return help;
    }
}
