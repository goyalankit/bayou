package com.ut.bayou;

public enum Commands {


    START("Start Bayou"),
    EXIT("exit this system"),
    INVALID("Invalid Command"),
    PING("Status Check"),
    HELP("Bring up this menu"),
    JOIN("<i> Node i joins the system"),
    RECONNECT("<i> node i gets connected to the system"),
    BREAKCONNECTION("<i,j> Break the connection between Node i and Node j"),
    RECOVERCONNECTION("<i,j> Recover the connection between Node i and Node j"),
    LEAVE("<i> Node i leaves(retires from) the system."),
    PAUSE("pause the script"),
    CONTINUE("continue the script");

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
            help +=  "\n-> " + c.toString() + ": " +c.getDescription();
        }
      return help;
    }
}
