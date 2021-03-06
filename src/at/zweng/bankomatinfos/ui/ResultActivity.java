package at.zweng.bankomatinfos.ui;

import static at.zweng.bankomatinfos.util.Utils.showAboutDialog;
import static at.zweng.bankomatinfos.util.Utils.showChangelogDialog;

import java.util.Locale;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ShareActionProvider;
import at.zweng.bankomatinfos.AppController;
import at.zweng.bankomatinfos2.R;

// TODO: maybe also add share action for general and transations fragment

/**
 * Activity for displaying the results (hosts fragements in tabs).
 * 
 * @author Johannes Zweng <johannes@zweng.at>
 */
public class ResultActivity extends FragmentActivity implements
		ActionBar.TabListener {

	private static AppController _controller = AppController.getInstance();
	private Fragment _fragmentResultInfos;
	private Fragment _fragmentResultEmxTxList;
	private Fragment _fragmentResultQuickTxList;
	private Fragment _fragmentResultLog;
	private boolean _showQuickLog;
	private boolean _showEmvLog;
	private int _numLogTabs;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	private SectionsPagerAdapter _sectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	private ViewPager _viewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result);
		_showQuickLog = (_controller.getCardInfoNullSafe(this).getQuickLog()
				.size() > 0);
		_showEmvLog = _controller.getCardInfoNullSafe(this).isEmvCard();
		if (_showEmvLog && _showQuickLog) {
			_numLogTabs = 2;
		} else {
			_numLogTabs = 1;
		}
		_fragmentResultInfos = new ResultInfosListFragment();
		if (_showEmvLog) {
			_fragmentResultEmxTxList = new ResultEmvTxListFragment();
		}
		if (_showQuickLog) {
			_fragmentResultQuickTxList = new ResultQuickTxListFragment();
		}
		_fragmentResultLog = new ResultLogFragment();

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the
		// primary sections of the app.
		_sectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		_viewPager = (ViewPager) findViewById(R.id.pager);
		_viewPager.setAdapter(_sectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		_viewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < _sectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(_sectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		// Locate MenuItem with ShareActionProvider
		MenuItem item = menu.findItem(R.id.action_share);
		// Fetch and store ShareActionProvider
		ShareActionProvider shareActionProvider = (ShareActionProvider) item
				.getActionProvider();

		// set the log content as share content
		Intent shareIntent = new Intent();
		shareIntent.setAction(Intent.ACTION_SEND);
		shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				getResources().getString(R.string.action_share_subject));
		shareIntent.putExtra(Intent.EXTRA_TEXT, AppController.getInstance()
				.getLog());
		shareIntent.setType("text/plain");
		shareActionProvider.setShareIntent(shareIntent);
		return true;
	}

	/**
	 * Called whenever we call invalidateOptionsMenu()
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// show share action only on Tab 2 (Log)
		// (tab index starts with 0)
		if (_viewPager.getCurrentItem() == 2 && _numLogTabs == 1) {
			menu.findItem(R.id.action_share).setVisible(true);
		} else if (_viewPager.getCurrentItem() == 3 && _numLogTabs == 2) {
			menu.findItem(R.id.action_share).setVisible(true);
		} else {
			menu.findItem(R.id.action_share).setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_about:
			showAboutDialog(getFragmentManager());
			return true;
		case R.id.action_changelog:
			showChangelogDialog(getFragmentManager(), true);
			return true;
		case R.id.action_settings:
			Intent i = new Intent();
			i.setComponent(new ComponentName(getApplicationContext(),
					SettingsActivity.class));
			startActivity(i);
			return true;
		}
		return false;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		_viewPager.setCurrentItem(tab.getPosition());
		invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			if (position == 0) {
				return _fragmentResultInfos;
			} else if (position == 1 && _showEmvLog) {
				return _fragmentResultEmxTxList;
			} else if (position == 1 && !_showEmvLog && _showQuickLog) {
				return _fragmentResultQuickTxList;
			} else if (position == 2 && _showEmvLog && _showQuickLog) {
				return _fragmentResultQuickTxList;
			} else {
				return _fragmentResultLog;
			}
		}

		@Override
		public int getCount() {
			return 2 + _numLogTabs;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale locale = Locale.getDefault();

			if (position == 0) {
				return getString(R.string.title_section_infos).toUpperCase(locale);
			} else if (position == 1 && _showEmvLog) {
				return getString(R.string.title_section_emv_logs).toUpperCase(locale);
			} else if (position == 1 && !_showEmvLog && _showQuickLog) {
				return getString(R.string.title_section_quick_logs).toUpperCase(locale);
			} else if (position == 2 && _showEmvLog && _showQuickLog) {
				return getString(R.string.title_section_quick_logs).toUpperCase(locale);
			} else {
				return getString(R.string.title_section_debug_log).toUpperCase(locale);
			}
		}
	}

}
