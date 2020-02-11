package id.ac.ui.clab.dchronochat.file;

import id.ac.ui.clab.dchronochat.chat.ChatbufProto;

public class Entity {
    private ChatbufProto.ChatMessage message;
    private float[] progress = new float[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private float time;
    private float speed;
    private boolean isSender;
    private boolean isFinish = false;

    public Entity() {

    }

    public Entity(ChatbufProto.ChatMessage message) {
        this.message = message;
    }

    public ChatbufProto.ChatMessage getMessage() {
        return message;
    }

    public ChatbufProto.ChatMessage.ChatMessageType getType() {
        return message.getType();
    }

    public String getData() {
        return message.getData();
    }

    public void setProgress(int index, float value) {
        progress[index] = value;
    }

    public int getProgress() {
        float pro = 0.f;
        for (float p : progress) {
            pro += p;
        }
        return (int) pro;
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public boolean isSender() {
        return isSender;
    }

    public void setSender() {
        isSender = true;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public void setFinish() {
        isFinish = true;
    }
}
