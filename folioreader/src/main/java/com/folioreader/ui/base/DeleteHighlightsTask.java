package com.folioreader.ui.base;

import android.os.AsyncTask;

import com.folioreader.model.HighLight;
import com.folioreader.model.sqlite.HighLightTable;

import java.util.List;

/**
 * Background task to delete highlights.
 * <p>
 * Created by tuan nguyen on 25/04/23.
 */
public class DeleteHighlightsTask extends AsyncTask<Void, Void, Void> {

    public DeleteHighlightsTask() {
    }

    @Override
    protected Void doInBackground(Void... voids) {
        HighLightTable.deleteAll();

        return null;
    }
}
