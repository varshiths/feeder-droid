package com.ssl.mavericks.feeder39;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidGridAdapter;

public class CaldroidCustom extends CaldroidFragment {

    @Override
    public CaldroidGridAdapter getNewDatesGridAdapter(int month, int year) {
        // TODO Auto-generated method stub
        return new CaldroidCustomAdapter(getActivity(), month, year, getCaldroidData(), extraData);
    }



}
