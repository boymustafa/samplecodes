package boy.dekatku.com.kpmg.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import boy.dekatku.com.kpmg.AppController;
import boy.dekatku.com.kpmg.Constants;
import boy.dekatku.com.kpmg.Activity.DashboardActivity;
import boy.dekatku.com.kpmg.Interface.FragmentInterface;
import boy.dekatku.com.kpmg.R;
import boy.dekatku.com.kpmg.Models.SEModel;
import boy.dekatku.com.kpmg.Helpers.Utils;

/**
 * Created by Boy on 7/28/15.
 */
public class KpmgFragment extends Fragment {

    FragmentInterface listener;

    String catName;

    GridAdapter adapter;
    GridView gvExample;
    ProgressDialog pDialog;
    private Response.Listener<JSONArray> jsonArrayListener;
    private Response.ErrorListener errorListener;

    ArrayList<SEModel> mModelList = new ArrayList<>();

    public KpmgFragment(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        appCompatActivity.getSupportActionBar().setHomeButtonEnabled(true);

        View rootView = inflater.inflate(R.layout.fragment_kpmg,container,false);

        listener.setTitle("KPMG SE Mobile");

        if (mModelList.size() == 0) {
            initListener();
            gvExample = (GridView) rootView.findViewById(R.id.gv1);
            adapter = new GridAdapter(getActivity().getBaseContext(), mModelList);
            gvExample.setAdapter(adapter);
            pDialog = new ProgressDialog(getActivity().getWindow().getContext());
            pDialog.show();
        } else {
            gvExample = (GridView) rootView.findViewById(R.id.gv1);
            adapter = new GridAdapter(getActivity().getBaseContext(), mModelList);
            gvExample.setAdapter(adapter);
        }
        gvExample.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int catId = Integer.parseInt(mModelList.get(position).getCatID());
                AppController.getInstance().setCategoryID(catId);
                Toast.makeText(getActivity().getBaseContext(), "position: " + position+" cat ID = "+catId, Toast.LENGTH_SHORT).show();
                listener.callLayout(Constants.categoryLayout);
            }
        });

        return rootView;
    }

    private void initListener(){

        jsonArrayListener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {

                pDialog.dismiss();

                System.out.println("****** KPMG FRAGMENT response is: " + jsonArray.toString());
                try {
                    JSONObject response = (JSONObject) jsonArray.get(0);
                    JSONArray dataArray = response.getJSONArray("Data");
                    for (int i = 0; i < dataArray.length(); i++) {
                        SEModel m = new SEModel();


                        m.setDescription(dataArray.getJSONObject(i).getString("category_description"));
                        m.setCatDate(dataArray.getJSONObject(i).getString("category_add_date"));
                        m.setCatPic(String.valueOf(dataArray.getJSONObject(i).get("category_picture")));
                        m.setMessage(dataArray.getJSONObject(i).getString("message"));
                        m.setCatName(dataArray.getJSONObject(i).getString("category_name"));
                        m.setCatIcon(dataArray.getJSONObject(i).getString("category_icon"));
                        m.setStatus(dataArray.getJSONObject(i).getString("category_status"));
                        m.setCatID(dataArray.getJSONObject(i).getString("category_id"));

                        AppController.getInstance().setCatName(dataArray.getJSONObject(i).getString("category_name"));

                        mModelList.add(m);


                    }


                } catch (JSONException ex) {
                    ex.printStackTrace();
                }


                adapter.notifyDataSetChanged();

            }
        };

        errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();
                error.getMessage();
                error.getCause();
                error.printStackTrace();
                System.out.print("---------------------------- error bro");
                if(error instanceof NoConnectionError)
                    Toast.makeText(getActivity().getBaseContext(), "No internet available/server down", Toast.LENGTH_SHORT).show();
                else if(error instanceof ServerError)
                    Toast.makeText(getActivity().getBaseContext(), "server error", Toast.LENGTH_SHORT).show();
                else if(error instanceof ParseError)
                    Toast.makeText(getActivity().getBaseContext(), "ParseError", Toast.LENGTH_SHORT).show();

            }
        };

        Utils.sendRequest(Constants.listMainCategoryUrl, getActivity().getBaseContext(), jsonArrayListener, errorListener);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try{
            listener = (FragmentInterface) activity;
        }catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + " must implement method");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.home:
                return false;
            case android.R.id.home:
//                Toast.makeText(getActivity().getBaseContext(), "Tet bro", Toast.LENGTH_SHORT).show();
                System.out.println("-------------------- you clicked home button bro");
                Intent backIntent = new Intent(getActivity().getBaseContext(),DashboardActivity.class);
                getActivity().startActivity(backIntent);

                return true;
            default:
                break;
        }
        return false;
    }

    public class GridAdapter extends BaseAdapter {


        private Context context;
        private ArrayList<SEModel> mCategoryList;


        public GridAdapter(Context context, ArrayList<SEModel> categoryList){
            this.context = context;
            this.mCategoryList = categoryList;
        }

        @Override
        public int getCount() {
            return mCategoryList.size();
        }

        @Override
        public Object getItem(int position) {
            return mCategoryList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder holder;

            if(row==null){
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.grid_items,parent,false);
                holder = new ViewHolder(row);
                row.setTag(holder);
            }else {
                holder = (ViewHolder) row.getTag();
            }

            byte[] decodedString = Base64.decode(mCategoryList.get(position).getCatIcon(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString,0, decodedString.length);



            holder.imageIcon.setImageBitmap(decodedByte);
            holder.imageIcon.setTag(Integer.parseInt(mCategoryList.get(position).getCatID()));
            holder.textTitle.setText(mCategoryList.get(position).getCatName());

            return row;
        }

        class ViewHolder{
            ImageView imageIcon;
            TextView textTitle;

            ViewHolder(View v){
                imageIcon = (ImageView) v.findViewById(R.id.iconGridd);
                textTitle = (TextView) v.findViewById(R.id.titleGrid);
            }

        }

    }

}