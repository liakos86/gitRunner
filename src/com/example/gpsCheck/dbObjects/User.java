package com.example.gpsCheck.dbObjects;

import android.content.SharedPreferences;

import java.util.List;

/**
 * Created by liakos on 22/4/2015.
 */
public class User {

    private String username;
    private ObjectId _id;
    private float totalDistance;
    private long totalTime;
    private int totalRuns; //from device db
    private int totalChallenges;
    private int totalScore;
    private List<Running> challenges;
    private String friends; //email list as it is unique
    private String email;
    private String friendRequests;

    public User(){}

    public User(
//             float totalDistance,
//             long totalTime,
            int totalRuns,
//             int totalChallenges,
            int totalScore,
            List<Running> challenges){
        this.challenges = challenges;
        this.totalRuns = totalRuns;
        this.totalScore = totalScore;

    }

    public User(SharedPreferences prefs){
        this._id = new ObjectId(prefs.getString("mongoId",null));
        this.username = prefs.getString("username", "");
        this.totalChallenges = prefs.getInt("totalChallenges", 0);
        this.totalDistance = prefs.getFloat("totalDistance", 0);
        this.totalScore = prefs.getInt("totalScore",0);
        this.totalTime = prefs.getLong("totalTime",0);
        this.friends = prefs.getString("friends","");
        this.friendRequests = prefs.getString("friendRequests","");
    }

    public String getFriendRequests() {
        return friendRequests;
    }

    public void setFriendRequests(String friendRequests) {
        this.friendRequests = friendRequests;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public float getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(float totalDistance) {
        this.totalDistance = totalDistance;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public int getTotalRuns() {
        return totalRuns;
    }

    public void setTotalRuns(int totalRuns) {
        this.totalRuns = totalRuns;
    }

    public int getTotalChallenges() {
        return totalChallenges;
    }

    public void setTotalChallenges(int totalChallenges) {
        this.totalChallenges = totalChallenges;
    }

    public List<Running> getChallenges() {
        return challenges;
    }

    public void setChallenges(List<Running> challenges) {
        this.challenges = challenges;
    }

    public String getFriends() {
        return friends;
    }

    public void setFriends(String friends) {
        this.friends = friends;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
