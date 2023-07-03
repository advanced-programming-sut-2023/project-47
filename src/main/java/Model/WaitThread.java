package Model;

public class WaitThread extends Thread {

    private int waitTime;

    public WaitThread(int waitTime){
        this.waitTime=waitTime;
    }
    
    @Override public void run(){
        try {
            this.sleep(waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

