package com.example.gpsCheck;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.example.gpsCheck.dbObjects.Running;
import com.example.gpsCheck.model.ContentDescriptor;
import com.example.gpsCheck.model.Database;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by liakos on 11/4/2015.
 */
public class FrgShowRuns  extends Fragment {

    static final String[] FROM = {
            // ! beware. I mark the position of the fields
            ContentDescriptor.Running.Cols.DESCRIPTION,
            ContentDescriptor.Running.Cols.DATE,
            ContentDescriptor.Running.Cols.ID,
            ContentDescriptor.Running.Cols.TIME,
            ContentDescriptor.Running.Cols.DISTANCE


    };
    static final int sDescPosition = 0;
    static final int sDatePosition = 1;
    static final int sIdPosition = 2;
    static final int sTimePosition = 3;
    static final int sDistPosition = 4;



    List<Running> runnings;
    ListView runningListView;
    ArrayAdapterItem adapter;
    Database db;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.showruns_frg, container, false);

        db = new Database(getActivity().getBaseContext());

        setList(v);


        return  v;
    }
    
    private void setList(View v){

        runnings = new ArrayList<Running>();
        runningListView = (ListView) v.findViewById(R.id.listRunning);
        runningListView.setDivider(null);

        runnings = fetchFromDb();


        adapter = new ArrayAdapterItem(getActivity().getApplicationContext(),
                R.layout.list_running_row, runnings);
        runningListView.setAdapter(adapter);
        
    }
    
    
    static FrgShowRuns init(int val) {
        FrgShowRuns truitonList = new FrgShowRuns();

        // Supply val input as an argument.
        Bundle args = new Bundle();
        args.putInt("val", val);
        truitonList.setArguments(args);

        return truitonList;
    }

    private List<Running> fetchFromDb() {


        // trTime = "app";

        Cursor c = getActivity().getContentResolver().query(
                ContentDescriptor.Running.CONTENT_URI,
                FROM, null, null, null);

        // Cursor c =
        // getContentResolver().query(ContentDescriptor.Running.CONTENT_URI,
        // proj,
        // ContentDescriptor.Running.Cols.PROFILE_ID+" = "+pr.getProfileId(),
        // null, ContentDescriptor.Running.Cols.ID+" DESC");

        List<Running> St = new ArrayList<Running>();

        if (c.getCount() > 0) {

            while (c.moveToNext()) {



                St.add(new Running(c.getLong(sIdPosition), c
                        .getString(sDescPosition), c.getString(sTimePosition),
                        c.getString(sDatePosition),  c.getString(sDistPosition)));
            }
        }
        c.close();
        c = null;

        return St;

    }



    // here's our beautiful adapter
    public class ArrayAdapterItem extends ArrayAdapter<Running> {

        Context mContext;
        int layoutResourceId;
        List<Running> data;

        public ArrayAdapterItem(Context mContext, int layoutResourceId,
                                List<Running> data) {

            super(mContext, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.mContext = mContext;
            this.data = data;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            runningViewHolder holder =null;
            if (convertView == null || !(convertView.getTag() instanceof runningViewHolder)) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_running_row, parent, false);

                holder = new runningViewHolder();

                holder.delete = (ImageView) convertView.findViewById(R.id.deleteRunning);
                holder.description = (TextView) convertView
                        .findViewById(R.id.profile_name);
                holder.time =  (TextView) convertView
                        .findViewById(R.id.profile_info);
//                holder.catImage = (ImageView) convertView
//                        .findViewById(R.id.trImage);



                convertView.setTag(holder);
            } else {
                holder = (runningViewHolder) convertView.getTag();

            }

            // object item based on the position
            final Running run = data.get(position);


            if (run.getDescription().length()>0)
                holder.description.setText(run.getDescription()+" "+run.getDistance()+" km");
            else holder.description.setText("-- No Description --");
//            DecimalFormat df = new DecimalFormat();
//            df.setMaximumFractionDigits(2);


            holder.time.setText(run.getTime() + " achieved @ " + run.getDate().substring(0,4)+"/"+run.getDate().substring(4,6)+"/"+run.getDate().substring(6,8));

            holder.delete.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    confirmDelete(run.getRunning_id(), position);
                }
            });

            return convertView;

        }

    }

    private class runningViewHolder{
        TextView description;
        TextView time;
        ImageView delete;
//        ImageView catImage;
    }




    private void confirmDelete(final Long trId,final int position){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());
        alertDialogBuilder.setTitle("Confirm")
                .setMessage("Delete Running ?")
                .setCancelable(false)
                .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

//                        deleteRunning(trId, position);
                    }

                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();


    }



}
