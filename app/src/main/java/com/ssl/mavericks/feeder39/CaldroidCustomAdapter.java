package com.ssl.mavericks.feeder39;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidGridAdapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import hirondelle.date4j.DateTime;

public class CaldroidCustomAdapter extends CaldroidGridAdapter {

    public CaldroidCustomAdapter(Context context, int month, int year,
                                       Map<String, Object> caldroidData,
                                       Map<String, Object> extraData) {
        super(context, month, year, caldroidData, extraData);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View cellView = convertView;

        // For reuse
        if (convertView == null) {
            cellView = inflater.inflate(R.layout.custom_cell, null);
        }

        int topPadding = cellView.getPaddingTop();
        int leftPadding = cellView.getPaddingLeft();
        int bottomPadding = cellView.getPaddingBottom();
        int rightPadding = cellView.getPaddingRight();

        TextView dateDay = (TextView) cellView.findViewById(R.id.number);
        TextView eventsDay = (TextView) cellView.findViewById(R.id.events_day);

        dateDay.setTextColor(Color.BLACK);

        // Get dateTime of this cell
        DateTime dateTime = this.datetimeList.get(position);
        Resources resources = context.getResources();

        // Set color of the dates in previous / next month
        if (dateTime.getMonth() != month) {
            dateDay.setTextColor(resources
                    .getColor(com.caldroid.R.color.caldroid_darker_gray));
        }

        boolean shouldResetDiabledView = false;
        boolean shouldResetSelectedView = false;

        // Customize for disabled dates and date outside min/max dates
        if ((minDateTime != null && dateTime.lt(minDateTime))
                || (maxDateTime != null && dateTime.gt(maxDateTime))
                || (disableDates != null && disableDates.indexOf(dateTime) != -1)) {

            dateDay.setTextColor(CaldroidFragment.disabledTextColor);
            if (CaldroidFragment.disabledBackgroundDrawable == -1) {
                cellView.setBackgroundResource(com.caldroid.R.drawable.disable_cell);
            } else {
                cellView.setBackgroundResource(CaldroidFragment.disabledBackgroundDrawable);
            }

            if (dateTime.equals(getToday())) {
                cellView.setBackgroundResource(com.caldroid.R.drawable.red_border_gray_bg);
            }

        } else {
            shouldResetDiabledView = true;
        }

        // Customize for selected dates
        if (selectedDates != null && selectedDates.indexOf(dateTime) != -1) {
            cellView.setBackgroundColor(resources
                    .getColor(com.caldroid.R.color.caldroid_sky_blue));

            dateDay.setTextColor(Color.BLACK);

        } else {
            shouldResetSelectedView = true;
        }

        if (shouldResetDiabledView && shouldResetSelectedView) {
            // Customize for today
            if (dateTime.equals(getToday())) {
                cellView.setBackgroundResource(com.caldroid.R.drawable.red_border);
            } else {
                cellView.setBackgroundResource(com.caldroid.R.drawable.cell_bg);
            }
        }
        eventsDay.setTextSize(8f); // dont touch
        dateDay.setTextSize(13f);  // dont touch
        eventsDay.setText("");

        HashMap<Date, ArrayList<Assignment>> listAss = UserActivity.assignmentsDateLists;

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

//        String dateStr = formatter.format(dateTime);
//        System.out.println(dateStr);
        Date p = null;
        //            p = formatter.parse(dateStr);

        if(listAss != null){
            if(listAss.get(dateTime) != null){
                dateDay.setVisibility(View.GONE);
                cellView.setBackgroundResource(com.caldroid.R.drawable.disabled_cell_dark);

                System.out.println(dateTime);
                String innerString = new String();

                for (Assignment ass : listAss.get(dateTime)){
                    innerString = innerString.concat(ass.getTitle() + "\n");
                }

                eventsDay.setText(innerString);

                eventsDay.setHorizontallyScrolling(true);
                eventsDay.setTextColor(Color.WHITE);
            }else {
                dateDay.setText("" + dateTime.getDay());
            }

        }else{
            dateDay.setText("" + dateTime.getDay());
        }


//        if(dateTime.equals(getToday())){
//            dateDay.setVisibility(View.GONE);
//            cellView.setBackgroundResource(com.caldroid.R.drawable.disabled_cell_dark);
//
//            System.out.println(dateTime);
//
//            eventsDay.setText("Helszcc\nTscscahis\nacascxac\nsca");
//
//            eventsDay.setHorizontallyScrolling(true);
//            eventsDay.setTextColor(Color.WHITE);
//        }else {
//            dateDay.setText("" + dateTime.getDay());
//        }

        // Somehow after setBackgroundResource, the padding collapse.
        // This is to recover the padding
        cellView.setPadding(leftPadding, topPadding, rightPadding,
                bottomPadding);

        // Set custom color if required
        setCustomResources(dateTime, cellView, dateDay);

        return cellView;
    }

}
