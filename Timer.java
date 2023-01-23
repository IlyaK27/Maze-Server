/**
 * Final Game Timer Class
 * @Author Ilya Kononov
 * @Date = January 22 2023
 * This class is used for abilities to keep track of their cooldown without using a thread
 */

public class Timer {
    private double startTime; 
    private double timerLength;

    public void start(){
        this.startTime = System.currentTimeMillis();
    }
    public void setTimerLength(double timeLength){
        this.timerLength = timeLength;
    }
    public boolean finished(){
        double currentTime = System.currentTimeMillis();
        if(this.startTime == 0 || this.timerLength == 0){
            return true;
        }
        return (currentTime - this.startTime) >= timerLength;
    }
}