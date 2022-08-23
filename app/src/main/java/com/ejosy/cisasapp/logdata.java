package com.ejosy.cisasapp;

class logdata {
    private int id;
    private String logdate, logtime, logsignalv, logphoneno, logmsg, logloclat, logloclong;

    public logdata(String logdate, String logtime, String logsignalv, String logphoneno, String logmsg, String logloclat, String logloclong)
    {
        this.id = id;
        this.logdate = logdate;
        this.logtime = logtime;
        this.logsignalv = logsignalv;
        this.logphoneno = logphoneno;
        this.logmsg = logmsg;
        this.logloclat = logloclat;
        this.logloclong = logloclong;
    }

    public int getId() {
        return id;
    }
    public String getlogdate() {
        return logdate;
    }
    public String getlogtime() {
        return logtime;
    }
    public String getlogsignalv() {return logsignalv;  }
    public String getlogphoneno() {return logphoneno;  }
    public String getlogmsg() {return logmsg;  }
    public String getlogloclat() {return logloclat;  }
    public String getlogloclong() {return logloclong;  }

 }


