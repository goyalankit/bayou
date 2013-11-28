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
    CONTINUE("continue the script"),
    PLAYLIST("<add|update|delete|read> <CLIENT_NO> the song"),
    PRINTLOG("<i|> print log for node i or all the nodes if left blank."),
    PRINTSID("Print server id"),
    STARTENTROPY("Start entropy with "),
    PRINTSERVERPLAYLIST("Print playlist at a server"),
    ISOLATE("isolate a node"),
    DUMMY("print log"),
    PRINTCONNECTIONS("Print Connections"),
    DISCONNECTCLIENT("disconnect client"),
    RECONNECTCLIENT("reconnect client");

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
