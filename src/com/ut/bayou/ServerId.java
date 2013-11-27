package com.ut.bayou;

import java.io.Serializable;

public class ServerId implements Serializable {
    long iSTimestamp; //other server's current timestamp
    ServerId iSId; //other server's id timestamp
    int hrNumber; //human readable number

    ServerId(long iSTimestamp, ServerId iSId, int hrNumber){
        this.iSTimestamp = iSTimestamp;
        this.iSId = iSId;
        this.hrNumber = hrNumber;
    }

    public String stringify(){
        return iSTimestamp+Constants.SPACE+Constants.SPACE+hrNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerId)) return false;

        ServerId serverId = (ServerId) o;

        if (hrNumber != serverId.hrNumber) return false;
        if (iSTimestamp != serverId.iSTimestamp) return false;
        if (iSId != null ? !iSId.equals(serverId.iSId) : serverId.iSId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (iSTimestamp ^ (iSTimestamp >>> 32));
        result = 31 * result + (iSId != null ? iSId.hashCode() : 0);
        result = 31 * result + hrNumber;
        return result;
    }
}
