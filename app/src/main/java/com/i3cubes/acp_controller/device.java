package com.i3cubes.acp_controller;

public class device {
    String name;
    String mac;
    int drawable;
    String color="";
    double signal_level;
    boolean isBLE=true;

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName (){
        return name;
    }

    public int getSignalBars(){
        if(signal_level<-100){
            return 0;
        }
        else if(signal_level<-90){
            return 1;
        }
        else if(signal_level<-80){
            return 2;
        }
        else if(signal_level<-70){
            return 3;
        }
        else if(signal_level<-60){
            return 4;
        }
        else if (signal_level<-50){
            return 5;
        }
        else{
            return 5;
        }
    }
}
