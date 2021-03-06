package com.hitstreamr.hitstreamrbeta;

public interface PlayerServiceCallback {
    public void setPlayerView();
    public void callPurchase();
    public void updateCreditText(String credit);
    public void autoPlayNext();
    public void stopPlayer();
}