package sauerapps.betterbetterrx.features.journal;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.app.BaseActivity;
import sauerapps.betterbetterrx.features.SettingsActivity;
import sauerapps.betterbetterrx.features.journal.journalList.AddJournalEntryDialogFragment;
import sauerapps.betterbetterrx.features.journal.journalList.JournalListFragment;

public class JournalActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        JournalListFragment journalListFragment = JournalListFragment.newInstance(mEncodedEmail);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container_journal, journalListFragment)
                    .commit();
        }
    }


    public void showAddNewJournalEntry(View view) {

        DialogFragment dialog = AddJournalEntryDialogFragment.newInstance(mEncodedEmail);
        dialog.show(JournalActivity.this.getFragmentManager(), "AddJournalEntryDialogFragment");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.menu_base, menu);
        MenuItem sort = menu.findItem(R.id.action_sort);
        sort.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sort) {
            startActivity(new Intent(JournalActivity.this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}