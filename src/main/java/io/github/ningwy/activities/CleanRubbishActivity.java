package io.github.ningwy.activities;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import io.github.ningwy.R;

public class CleanRubbishActivity extends TabActivity {

    private TabHost host;

    private int[] icons = new int[]{R.drawable.clean_cache, R.drawable.clean_sdcard, };
    private int[] iconsLight = new int[]{R.drawable.clean_cache_on, R.drawable.clean_sdcard_on};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clean_rubbish);

        host = getTabHost();
        host.addTab(getCleanCacheTabSpec());
        host.addTab(getCleanSdCardTabSpec());

        host.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                TabWidget tabWidget = host.getTabWidget();
                for (int i = 0; i < tabWidget.getTabCount(); i++) {
                    View view = tabWidget.getChildAt(i);
                    ImageView iv_cr_icon = (ImageView) view.findViewById(R.id.iv_cr_icon);
                    TextView tv_cr_name = (TextView) view.findViewById(R.id.tv_cr_name);
                    if (i == host.getCurrentTab()) {
                        iv_cr_icon.setImageResource(iconsLight[i]);
                        tv_cr_name.setTextColor(getResources().getColor(R.color.ligthGreen));
                    } else {
                        iv_cr_icon.setImageResource(icons[i]);
                        tv_cr_name.setTextColor(getResources().getColor(R.color.lightGray));
                    }
                }

            }
        });

    }

    /**
     * 得到清除缓存的tabSpec
     * @return
     */
    public TabHost.TabSpec getCleanCacheTabSpec() {
        TabHost.TabSpec tabSpec = host.newTabSpec("cleanCache");
        Intent intent = new Intent(CleanRubbishActivity.this, CleanCacheActivity.class);
        tabSpec.setContent(intent);
        tabSpec.setIndicator(getView("清除缓存", R.color.ligthGreen, R.drawable.clean_cache_on));
        return tabSpec;
    }

    /**
     * 得到清除垃圾的tabSpec
     * @return
     */
    public TabHost.TabSpec getCleanSdCardTabSpec() {
        TabHost.TabSpec tabSpec = host.newTabSpec("cleanSdCard");
        Intent intent = new Intent(CleanRubbishActivity.this, CleanSdardActivity.class);
        tabSpec.setContent(intent);
        tabSpec.setIndicator(getView("清除垃圾", R.color.lightGray,  R.drawable.clean_sdcard));
        return tabSpec;
    }

    /**
     * 得到要显示在TabWight中tabSpec的视图
     * @param name tabSpec的名称
     * @param textColor tabSpec的名称的字体颜色
     * @param iconId tabSpec的icon的资源id
     * @return
     */
    public View getView(String name, int textColor, int iconId) {
        View view = View.inflate(CleanRubbishActivity.this, R.layout.tab_main_nav, null);
        ImageView iv_cr_icon = (ImageView) view.findViewById(R.id.iv_cr_icon);
        TextView tv_cr_name = (TextView) view.findViewById(R.id.tv_cr_name);
        iv_cr_icon.setImageResource(iconId);
        tv_cr_name.setText(name);
        tv_cr_name.setTextColor(getResources().getColor(textColor));
        return view;
    }
}
