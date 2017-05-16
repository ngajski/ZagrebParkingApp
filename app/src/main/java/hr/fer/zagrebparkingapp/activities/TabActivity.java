package hr.fer.zagrebparkingapp.activities;

import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.view.ViewPager;
import android.os.Bundle;

import hr.fer.zagrebparkingapp.R;
import hr.fer.zagrebparkingapp.adapter.SectionPageAdapter;
import hr.fer.zagrebparkingapp.fragments.CarsFragment;
import hr.fer.zagrebparkingapp.fragments.HistoryFragment;

public class TabActivity extends AppCompatActivity {

    private SectionPageAdapter pageAdapter;
    private ViewPager mViewPager;
    private String priority;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        pageAdapter = new SectionPageAdapter(getSupportFragmentManager());

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            priority = extras.getString("priority");
        }

        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPage(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    private void setupViewPage(ViewPager viewPager) {
        SectionPageAdapter adapter = new SectionPageAdapter(getSupportFragmentManager());

        if(priority != null) {
            if (priority.equals("Cars")) {
                adapter.addFragment(new CarsFragment(), "Vozila");
                adapter.addFragment(new HistoryFragment(), "Plaćanja");
            } else {
                adapter.addFragment(new HistoryFragment(), "Plaćanja");
                adapter.addFragment(new CarsFragment(), "Vozila");
            }
        } else {
            adapter.addFragment(new CarsFragment(), "Vozila");
            adapter.addFragment(new HistoryFragment(), "Plaćanja");
        }
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
    }
}
