package com.example.gpsCheck;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.example.gpsCheck.dbObjects.Running;
import com.example.gpsCheck.dbObjects.User;
import com.example.gpsCheck.model.ContentDescriptor;
import com.example.gpsCheck.model.Database;

import java.util.ArrayList;
import java.util.List;

//fixme commit

public class FrgShowChallenge extends BaseFragment {

    LeaderArrayAdapterItem adapter;
    RequestsArrayAdapterItem adapterForReq;
    ChallengesArrayAdapterItem adapterForChal;
    List<User>leaders;
    List<Running>challenges;
    List<String>friendRequests;
    EditText friendName;
    Button addFriend;

    SyncHelper sh;



    /** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.showchallenge_frg, container, false);
        sh = new SyncHelper(getActivity());





        setList(v);

        getLeaderBoardAndChallenges();


        return  v;
    }

    public void getLeaderBoardAndChallenges(){

        ExtApplication application = (ExtApplication) getActivity().getApplication().getApplicationContext();
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(application);

        String friends = app_preferences.getString("friends",null);


        if (((ActMainTest)getActivity()).isNetworkAvailable()) {
            if (friends != null && !friends.equals("")) {
                new getLeaderBoardOrFriend(getActivity(), friends, 0).execute();
            }

            if (app_preferences.getString("username", null) != null) {


                new getChallenges(getActivity()).execute();

            }
        } else {
            Toast.makeText(getActivity(), "Connection lost", Toast.LENGTH_LONG).show();
        }

    }

    private void setList(View v){

        addFriend = (Button) v.findViewById(R.id.buttonAddFriend);
        friendName = (EditText) v.findViewById(R.id.editNewFriend);

        friendRequests = new ArrayList<String>();
        Database db = new Database(getActivity());

        leaders = db.fetchLeadersFromDb();
        challenges = db.fetchRunsByTypeFromDb(1);


        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String[] friendList = app_preferences.getString("friendFRequests","").split(" ");
        for (String fr:friendList){
            if (!fr.equals("null")&& !fr.equals("") && !fr.equals(" ")) friendRequests.add(fr);
        }

        final ViewFlipper vs = (ViewFlipper) v.findViewById(R.id.chalSwitcher);

        Button leaderBoard = (Button) v.findViewById(R.id.buttonLeaders);
        Button friendReq = (Button) v.findViewById(R.id.buttonRequests);
        Button chals = (Button) v.findViewById(R.id.buttonChallenges);

        ListView friendsListView = (ListView) v.findViewById(R.id.listFriendRequests);
        friendsListView.setDivider(null);

        ListView runningListView = (ListView) v.findViewById(R.id.listLeaders);
        runningListView.setDivider(null);

        ListView chalsListView = (ListView) v.findViewById(R.id.listChallenges);
        chalsListView.setDivider(null);

        adapter = new LeaderArrayAdapterItem(getActivity().getApplicationContext(),
                R.layout.list_running_row, leaders);
        runningListView.setAdapter(adapter);


        adapterForReq = new RequestsArrayAdapterItem(getActivity().getApplicationContext(),
                R.layout.list_request_row, friendRequests);
        friendsListView.setAdapter(adapterForReq);

        adapterForChal = new ChallengesArrayAdapterItem(getActivity().getApplicationContext(),
                R.layout.list_challenge_row, challenges);
        chalsListView.setAdapter(adapterForChal);

        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              fetchFriend();
            }
        });

        leaderBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vs.setDisplayedChild(0);
            }
        });
        friendReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vs.setDisplayedChild(1);
            }
        });
        chals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vs.setDisplayedChild(2);
            }
        });




    }

    public void refreshRequests(){

        friendRequests.clear();
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());



        String[] friendList = app_preferences.getString("friendRequests","").split(" ");
        for (String fr:friendList){
            if (!fr.equals("null")&& !fr.equals("") && !fr.equals(" ")) friendRequests.add(fr);
        }
        adapterForReq.notifyDataSetChanged();
    }

    public void refreshChallengeAdapter(List<Running>new_challenges){

        challenges.clear();

        for(Running run:new_challenges){
            challenges.add(run);
        }


        adapterForChal.notifyDataSetChanged();
    }

    private void fetchFriend() {

        if (friendName.getText().length() > 3) {
            if (!alreadyFriend(friendName.getText().toString())) {
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(addFriend.getWindowToken(), 0);

                if (((ActMainTest)getActivity()).isNetworkAvailable()) {
                    new getLeaderBoardOrFriend(getActivity(), friendName.getText().toString(), 1).execute();
                }else{
                    Toast.makeText(getActivity(), "Please connect to the internet", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            Toast.makeText(getActivity(), "Insert valid name", Toast.LENGTH_LONG).show();
        }
    }

    private boolean alreadyFriend(String friendName){

        ExtApplication application = (ExtApplication) getActivity().getApplication().getApplicationContext();
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(application);
        String friends = app_preferences.getString("friends", null);
        if (friends != null && !friends.equals("")) {
            String[] friendsList = friends.split(" ");
            for (String friendEmail : friendsList) {
                if (friendEmail.equals(friendName)) {
                    Toast.makeText(getActivity(), "Already a friend", Toast.LENGTH_LONG).show();
                    return true;
                }
            }

        }
        return false;



    }


    private void setAdapter(List<User>users) {
        //todo maybe empty and refill

        leaders.clear();

        for (User user: users) leaders.add(user);
        int num = users.size();
        int score = user.getTotalScore();
        for (int i=0; i<num; i++){
            if (score>leaders.get(i).getTotalScore()){
                leaders.add(i,user);
                break;
            }
            if (i==num-1)
            leaders.add(num,user);
        }


//        leaders = users;
        adapter.notifyDataSetChanged();
    }



    static FrgShowChallenge init(int val) {
        FrgShowChallenge truitonList = new FrgShowChallenge();

        // Supply val input as an argument.
        Bundle args = new Bundle();
        args.putInt("val", val);
        truitonList.setArguments(args);

        return truitonList;
    }

    private class getLeaderBoardOrFriend extends AsyncTask<Void, Void, List<User>> {
        private Activity activity;
        String friends;
        int type;

        public getLeaderBoardOrFriend(Activity activity, String friends, int type) {
            this.activity = activity;
            this.friends = friends;
            this.type = type;
        }

        protected void onPreExecute() {
            addFriend.setClickable(false);
        }

        @Override
        protected List<User> doInBackground(Void... unused) {


            return sh.getLeaderBoardOrFriend(friends, type);


        }

        @Override
        protected void onPostExecute(List<User> users) {

            addFriend.setClickable(true);

            if (type==0)
                setAdapter(users);
            else if (type==1)
                Toast.makeText(getActivity(), "Friend request sent!", Toast.LENGTH_LONG).show();

        }

    }

    private class getChallenges extends AsyncTask<Void, Void, List<Running>> {
        private Activity activity;


        public getChallenges(Activity activity) {
            this.activity = activity;

        }

        protected void onPreExecute() {
            addFriend.setClickable(false);
        }

        @Override
        protected List<Running> doInBackground(Void... unused) {


            return sh.getMongoChallenges();


        }

        @Override
        protected void onPostExecute(List<Running> challenges) {

            addFriend.setClickable(true);


                refreshChallengeAdapter(challenges);


        }


    }


    private class acceptRequest extends AsyncTask<Void, Void, User> {
        private Activity activity;
        String friend;


        public acceptRequest(Activity activity, String friend) {
            this.activity = activity;
            this.friend = friend;
        }

        protected void onPreExecute() {
            addFriend.setClickable(false);
        }

        @Override
        protected User doInBackground(Void... unused) {



                return      sh.getMongoUserByUsernameForFriend(friend, 0);



        }

        @Override
        protected void onPostExecute(User user) {

            addFriend.setClickable(true);
            refreshRequests();
            getLeaderBoardAndChallenges();




        }


    }



    public class LeaderArrayAdapterItem extends ArrayAdapter<User> {

        Context mContext;
        int layoutResourceId;
        List<User> data;

        public LeaderArrayAdapterItem(Context mContext, int layoutResourceId,
                                List<User> data) {

            super(mContext, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.mContext = mContext;
            this.data = data;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            leaderViewHolder holder =null;
            if (convertView == null || !(convertView.getTag() instanceof leaderViewHolder)) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_leaderboard_row, parent, false);

                holder = new leaderViewHolder();

                holder.username = (TextView) convertView
                        .findViewById(R.id.leader_username);
                holder.score =  (TextView) convertView
                        .findViewById(R.id.leader_points);



                convertView.setTag(holder);
            } else {
                holder = (leaderViewHolder) convertView.getTag();

            }

            // object item based on the position
             User user1 = data.get(position);


            if (user1.getUsername().length()>0)
                holder.username.setText(user1.getUsername());
            else holder.username.setText("-- No name --");

            if (user1.getUsername().equals(user.getUsername())){
                holder.username.setTextColor(getResources().getColor(R.color.runner_green));
            }else{
                holder.username.setTextColor(getResources().getColor(R.color.drawer_black));

            }



            holder.score.setText(String.valueOf(user1.getTotalScore()));



            return convertView;

        }

    }

    public class RequestsArrayAdapterItem extends ArrayAdapter<String> {

        Context mContext;
        int layoutResourceId;
        List<String> data;

        public RequestsArrayAdapterItem(Context mContext, int layoutResourceId,
                                      List<String> data) {

            super(mContext, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.mContext = mContext;
            this.data = data;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            leaderViewHolder holder =null;
            if (convertView == null || !(convertView.getTag() instanceof leaderViewHolder)) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_request_row, parent, false);

                holder = new leaderViewHolder();

                holder.username = (TextView) convertView
                        .findViewById(R.id.friend_name);

                holder.add = (ImageView) convertView.findViewById(R.id.trImageAdd);


                convertView.setTag(holder);
            } else {
                holder = (leaderViewHolder) convertView.getTag();

            }

            // object item based on the position
          final  String friend= data.get(position);


          holder.username.setText(friend+" wants to add you as a friend!");
            holder.add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (((ActMainTest)getActivity()).isNetworkAvailable()) {
                        new acceptRequest(getActivity(), friend).execute();
                    }

                }
            });


            return convertView;

        }

    }

    public class ChallengesArrayAdapterItem extends ArrayAdapter<Running> {

        Context mContext;
        int layoutResourceId;
        List<Running> data;

        public ChallengesArrayAdapterItem(Context mContext, int layoutResourceId,
                                        List<Running> data) {

            super(mContext, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.mContext = mContext;
            this.data = data;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            leaderViewHolder holder =null;
            if (convertView == null || !(convertView.getTag() instanceof leaderViewHolder)) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_challenge_row, parent, false);

                holder = new leaderViewHolder();

                holder.username = (TextView) convertView
                        .findViewById(R.id.text_chal);

                holder.add = (ImageView) convertView.findViewById(R.id.opponentIcon);

                holder.score = (TextView) convertView.findViewById(R.id.text_respond);


                convertView.setTag(holder);
            } else {
                holder = (leaderViewHolder) convertView.getTag();

            }

            // object item based on the position
            final  Running run= data.get(position);

            if (run.getUser_name().equals(user.getUsername())){

                if (run.getStatus()==1){
                    if (run.getWinner()!=null&&run.getWinner().equals(run.getOpponent_name())){
                        holder.username.setText("LOOSER! "+run.getOpponent_name()+" won the challenge!");
                        holder.add.setImageDrawable(getResources().getDrawable(R.drawable.looser));
                    }else{
                        holder.username.setText("HOORAY! "+"You beat "+run.getOpponent_name());
                        holder.add.setImageDrawable(getResources().getDrawable(R.drawable.winner));

                    }

                }else{
                    holder.add.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));

                    holder.username.setText("Waiting for "+run.getOpponent_name()+" to respond");
                    holder.score.setVisibility(View.GONE);
                }


            }else{
                holder.username.setText(run.getUser_name()+" challenged you for "+run.getDistance()+" meters");
                holder.score.setVisibility(View.VISIBLE);
                holder.add.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));


                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        beginChallenge(run);


                    }
                });
            }


            return convertView;

        }

    }

    private class leaderViewHolder{
        TextView username;
        TextView score;
        ImageView add;
    }


    private void beginChallenge(Running run){


        ((ActMainTest) getActivity()).respondToChal(run);

    }
}


