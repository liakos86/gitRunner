package com.example.gpsCheck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

    TextView noDataImage;

    SharedPreferences app_preferences;



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

        ExtApplication application = (ExtApplication) getActivity().getApplication().getApplicationContext();
         app_preferences = PreferenceManager.getDefaultSharedPreferences(application);




        setList(v);

        getLeaderBoardAndChallenges();


        return  v;
    }

    public void getLeaderBoardAndChallenges(){


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


        noDataImage = (TextView) v.findViewById(R.id.noDataImg);



        addFriend = (Button) v.findViewById(R.id.buttonAddFriend);
        friendName = (EditText) v.findViewById(R.id.editNewFriend);

        friendRequests = new ArrayList<String>();
        Database db = new Database(getActivity());

        String friends = app_preferences.getString("friends",null);
        leaders = new ArrayList<User>();
        challenges = new ArrayList<Running>();


            if (friends != null && !friends.equals("")) {

                leaders = db.fetchLeadersFromDb();
                challenges = db.fetchRunsByTypeFromDb(1);
            }



        final String[] friendList = app_preferences.getString("friendFRequests","").split(" ");
        for (String fr:friendList){
            if (!fr.equals("null")&& !fr.equals("") && !fr.equals(" ")) friendRequests.add(fr);
        }

        final ViewFlipper vs = (ViewFlipper) v.findViewById(R.id.chalSwitcher);

        final Button leaderBoard = (Button) v.findViewById(R.id.buttonLeaders);
        final Button friendReq = (Button) v.findViewById(R.id.buttonRequests);
        final Button chals = (Button) v.findViewById(R.id.buttonChallenges);
        leaderBoard.setSelected(true);

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


        List<User>users2 = new ArrayList<User>();
        for (User user:leaders) {users2.add(user);}
        setAdapter(users2);

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
                leaderBoard.setSelected(true);
                friendReq.setSelected(false);
                chals.setSelected(false);

                if (leaders.size()==0){
                    noDataImage.setVisibility(View.VISIBLE);
                    vs.setVisibility(View.GONE);
                }else{
                    noDataImage.setVisibility(View.GONE);
                    vs.setVisibility(View.VISIBLE);
                }

            }
        });
        friendReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vs.setDisplayedChild(1);
                leaderBoard.setSelected(false);
                friendReq.setSelected(true);
                chals.setSelected(false);

                if (friendRequests.size()==0){
                    noDataImage.setVisibility(View.VISIBLE);
                    vs.setVisibility(View.GONE);

                }else{
                    noDataImage.setVisibility(View.GONE);
                    vs.setVisibility(View.VISIBLE);
                }

            }
        });
        chals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vs.setDisplayedChild(2);
                leaderBoard.setSelected(false);
                friendReq.setSelected(false);
                chals.setSelected(true);

                if (challenges.size()==0){
                    noDataImage.setVisibility(View.VISIBLE);
                    vs.setVisibility(View.GONE);
                }else{
                    noDataImage.setVisibility(View.GONE);
                    vs.setVisibility(View.VISIBLE);
                }
            }
        });




    }

    public void refreshRequests(){

        friendRequests.clear();



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

        String friends = app_preferences.getString("friends", null);
        String sentRequests = app_preferences.getString("sentRequests",null);
        if (friends != null && !friends.equals("")) {
            String[] friendsList = friends.split(" ");
            for (String friendEmail : friendsList) {
                if (friendEmail.equals(friendName)) {
                    Toast.makeText(getActivity(), "Already a friend", Toast.LENGTH_LONG).show();
                    return true;
                }
            }

        }
        if (sentRequests != null && !sentRequests.equals("")) {
            String[] sentList = sentRequests.split(" ");
            for (String friendEmail : sentList) {
                if (friendEmail.equals(friendName)) {
                    Toast.makeText(getActivity(), "Already sent request to "+friendEmail, Toast.LENGTH_LONG).show();
                    return true;
                }
            }

        }
        return false;



    }


    private void setAdapter(List<User>users) {
        //todo maybe empty and refill


        User user = new User();
        user.setTotalScore(app_preferences.getInt("totalScore",0));
        user.setUsername(app_preferences.getString("username", "you"));
        user.setTotalChallenges(app_preferences.getInt("totalChallenges", 0));
        user.setWonChallenges(app_preferences.getInt("wonChallenges", 0));






        leaders.clear();

        for (User userAdd: users) leaders.add(userAdd);
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

            if (type==0&&users.size()>0)
                setAdapter(users);
            else if (type==1) {
                if (users.size() == 1)
                    Toast.makeText(getActivity(), "Friend request sent!", Toast.LENGTH_LONG).show();
                else if (users.size() == 0)
                    Toast.makeText(getActivity(), "User does not exist!", Toast.LENGTH_LONG).show();
            }

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


            if (challenges.size()>0)
                refreshChallengeAdapter(challenges);


        }


    }


    private class acceptOrRejectRequest extends AsyncTask<Void, Void, User> {
        private Activity activity;
        String friend;
        int type;


        public acceptOrRejectRequest(Activity activity, String friend, int type) {
            this.activity = activity;
            this.friend = friend;
            this.type = type;
        }

        protected void onPreExecute() {
            addFriend.setClickable(false);
        }

        @Override
        protected User doInBackground(Void... unused) {



                return      sh.getMongoUserByUsernameForFriend(friend, type);



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
                holder.chals = (TextView) convertView
                        .findViewById(R.id.leader_chals);
                holder.score =  (TextView) convertView
                        .findViewById(R.id.leader_points);

                holder.add = (ImageView)convertView.findViewById(R.id.leader_image);



                convertView.setTag(holder);
            } else {
                holder = (leaderViewHolder) convertView.getTag();

            }

            // object item based on the position
             User user1 = data.get(position);

            if (position<3){
                if (position==0)
                    holder.add.setImageDrawable(getResources().getDrawable(R.drawable.first_32));
                else if (position==1)
                    holder.add.setImageDrawable(getResources().getDrawable(R.drawable.second_32));
                else
                    holder.add.setImageDrawable(getResources().getDrawable(R.drawable.third_32));
            }else{
                holder.add.setImageDrawable(null);
            }

            holder.chals.setText(user1.getWonChallenges()+" / "+user1.getTotalChallenges()+" challenges");


            if (user1.getUsername().length()>0)
                holder.username.setText(user1.getUsername());
            else holder.username.setText("-- No name --");

            if (user1.getUsername().equals(user.getUsername())){
                holder.username.setTextColor(getResources().getColor(R.color.runner_green));
            }else{
                holder.username.setTextColor(getResources().getColor(R.color.drawer_black));

            }



            holder.score.setText(String.valueOf(user1.getTotalScore())+" points");



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

                holder.add = (ImageView) convertView.findViewById(R.id.acceptFriend);
                holder.reject = (ImageView) convertView.findViewById(R.id.rejectFriend);


                convertView.setTag(holder);
            } else {
                holder = (leaderViewHolder) convertView.getTag();

            }

            // object item based on the position
          final  String friend= data.get(position);


          holder.username.setText(friend+" wants to add you as a friend");
            holder.add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (((ActMainTest)getActivity()).isNetworkAvailable()) {
                        new acceptOrRejectRequest(getActivity(), friend, 0).execute();
                    }

                }
            });

            holder.reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (((ActMainTest)getActivity()).isNetworkAvailable()) {
                        new acceptOrRejectRequest(getActivity(), friend, 2).execute();
                    }

                }
            });


            return convertView;

        }

    }

    private void confirmDeleteChallenge(final String m_id){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());

        // set title
        alertDialogBuilder.setTitle("Delete Challenge");

        // set dialog message
        alertDialogBuilder
                .setMessage("Are you sure?")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {


                        deleteChallenge(m_id);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {


                       dialog.dismiss();
                    }
                });


        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void deleteChallenge(String runId){
        Database db = new Database(getActivity());
        db.deleteChallenge(runId);
        List<Running>challengesNew = db.fetchRunsByTypeFromDb(1);

        refreshChallengeAdapter(challengesNew);
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


            if (run.getUser_name().equals(app_preferences.getString("username",""))){

                if (run.getStatus()==1){
                    if (run.getWinner()!=null&&run.getWinner().equals(run.getOpponent_name())){
                        holder.username.setText("LOOSER! "+run.getOpponent_name()+" won the challenge!");
                        holder.add.setImageDrawable(getResources().getDrawable(R.drawable.ic_looser_32));
                    }else{
                        holder.username.setText("HOORAY! "+"You beat "+run.getOpponent_name());
                        holder.username.setText("HOORAY! "+"You beat "+run.getOpponent_name());
                        holder.add.setImageDrawable(getResources().getDrawable(R.drawable.ic_winner_32));

                    }

                    holder.score.setTextColor(getResources().getColor(R.color.runner_red));

                    holder.score.setText("Touch to delete");

                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {


                            confirmDeleteChallenge(run.getMongoId());


                        }
                    });


                }else{
                    holder.add.setImageDrawable(getResources().getDrawable(R.drawable.ic_waiting_32));

                    holder.username.setText("Waiting for "+run.getOpponent_name()+" to respond in "+String.format("%1$,.0f", (run.getDistance()))+" meters");
                    holder.score.setTextColor(getResources().getColor(R.color.runner_dark_blue));

                    holder.score.setText("Touch to see route");

                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            beginChallenge(run, 1);
                        }
                    });
                }


            }else{
                holder.username.setText(run.getUser_name()+" challenged you for "+String.format("%1$,.0f", (run.getDistance())) + " meters");
                holder.score.setText("Touch to respond");
                holder.score.setTextColor(getResources().getColor(R.color.runner_green));
                holder.add.setImageDrawable(getResources().getDrawable(R.drawable.ic_waiting_me_32));


                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        beginChallenge(run, 0);


                    }
                });
            }


            return convertView;

        }

    }

    private class leaderViewHolder{
        TextView username;
        TextView score;
        TextView chals;
        ImageView add;
        ImageView reject;
    }


    private void beginChallenge(Running run, int type){


        ((ActMainTest) getActivity()).respondToChal(run, type);

    }
}


