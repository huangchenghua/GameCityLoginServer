package com.gz.gamecity.login;


public class LoginMain {

	public static void main(String[] args) {
		
		Thread shutdownHook = new Thread() {  
            public void run() {  
            	LoginServiceMain.getInstance().stopServer(); 
            }  
        };  
        Runtime.getRuntime().addShutdownHook(shutdownHook);  
        
		LoginServiceMain.getInstance().startServer();
	}

}
