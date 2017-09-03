package com.ess.konachan.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ess.konachan.R;
import com.ess.konachan.bean.SearchBean;
import com.ess.konachan.global.Constants;
import com.ess.konachan.http.OkHttp;
import com.ess.konachan.utils.FileUtils;
import com.ess.konachan.utils.StringUtils;
import com.ess.konachan.utils.UIUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

public class SearchActivity extends AppCompatActivity {

    private Button mBtnSearchChinese;
    private EditText mEtSearchChinese;
    private Button mBtnSearchTags;
    private EditText mEtSearchTags;
    private Button mBtnSearchId;
    private EditText mEtSearchId;
    private Button mBtnSearchAdvanced;
    private EditText mEtSearchAdvanced;

    // 存储着K站所有的tag，用于搜索提示
    private ArrayList<SearchBean> mSearchList = new ArrayList<>();

    // 当前搜索内容的下拉提示内容
    private LinkedHashMap<String, Integer> mPromptMap = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initView();
        getSearchList();
    }

    private void initView() {
        mBtnSearchChinese = (Button) findViewById(R.id.btn_search_chinese);
        mBtnSearchChinese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chineseName = mEtSearchChinese.getText().toString().trim();
                Intent intent = new Intent();
                intent.putExtra(Constants.SEARCH_TAG, chineseName);
                setResult(Constants.SEARCH_CODE_CHINESE, intent);
                UIUtils.closeSoftInput(SearchActivity.this);
                finish();
            }
        });

        mEtSearchChinese = (EditText) findViewById(R.id.et_search_chinese);
        mEtSearchChinese.setFilters(new InputFilter[]{new InputFilter() {

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                Pattern pattern = Pattern.compile("[\u4e00-\u9fa5·]+");
                return StringUtils.filter(source.toString(), pattern);
            }
        }});

        mBtnSearchTags = (Button) findViewById(R.id.btn_search_tags);
        mBtnSearchTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tags = mEtSearchTags.getText().toString().trim();
                Intent intent = new Intent();
                intent.putExtra(Constants.SEARCH_TAG, tags);
                setResult(Constants.SEARCH_CODE_TAGS, intent);
                UIUtils.closeSoftInput(SearchActivity.this);
                finish();
            }
        });

        mEtSearchTags = (EditText) findViewById(R.id.et_search_tags);
        mEtSearchTags.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                return source.toString().replace(" ", "_");
            }
        }});
        mEtSearchTags.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO 完善搜索提示（现在与K站算法不完全一样）
                if (!TextUtils.isEmpty(s)) {
                    mPromptMap.clear();
                    getTagList(s.toString());
                    for (String prompt : mPromptMap.keySet()) {
                        Log.i("rrr", prompt);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mBtnSearchId = (Button) findViewById(R.id.btn_search_id);
        mBtnSearchId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tags = mEtSearchId.getText().toString().trim();
                Intent intent = new Intent();
                intent.putExtra(Constants.SEARCH_TAG, tags);
                setResult(Constants.SEARCH_CODE_ID, intent);
                UIUtils.closeSoftInput(SearchActivity.this);
                finish();
            }
        });

        mEtSearchId = (EditText) findViewById(R.id.et_search_id);

        mBtnSearchAdvanced = (Button) findViewById(R.id.btn_search_advanced);
        mBtnSearchAdvanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tags = mEtSearchAdvanced.getText().toString().trim();
                Intent intent = new Intent();
                intent.putExtra(Constants.SEARCH_TAG, tags);
                setResult(Constants.SEARCH_CODE_ADVANCED, intent);
                UIUtils.closeSoftInput(SearchActivity.this);
                finish();
            }
        });

        mEtSearchAdvanced = (EditText) findViewById(R.id.et_search_advanced);
    }

    // 获取json文件里所有的搜索标签
    private void getSearchList() {
        String name = "";
        String path = getFilesDir().getPath();
        String searchMode = OkHttp.getSearchModeUrl(this);
        switch (searchMode) {
            case Constants.BASE_URL_SAFE_MODE:
                name = FileUtils.encodeMD5String(Constants.SAFE_MODE_TAG_JSON_URL);
                break;
            case Constants.BASE_URL_R18_MODE:
                name = FileUtils.encodeMD5String(Constants.R18_MODE_TAG_JSON_URL);
                break;
        }

        File file = new File(path, name);
        if (file.exists()) {
            String json = FileUtils.fileToString(file);
            json = json == null ? "" : json;
            try {
                String data = new JSONObject(json).getString("data");
                String[] tags = data.split(" ");
                for (String tag : tags) {
                    String[] details = tag.split("`");
                    SearchBean searchBean = new SearchBean(details[0]);
                    searchBean.tagList.addAll(Arrays.asList(details).subList(1, details.length));
                    mSearchList.add(searchBean);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // 筛选当前搜索内容的提示标签，最多10个
    // 去除"_" "-"等连字符
    private void getTagList(String search) {
        search = search.replaceAll("_|-", "");
        if (!TextUtils.isEmpty(search)) {
            int length = search.length();
            for (int i = 0; i <= length; i++) {
                String start = search.substring(0, length - i).toLowerCase();
                String contain = search.substring(length - i).toLowerCase();
                filter(start, contain);
                if (mPromptMap.size() >= 10) {
                    break;
                }
            }
        }
    }

    // 层级筛选
    // 例：搜索fla，筛选顺序为
    // startWith("fla")
    // -> startWidth("fl"), contains("a")
    // -> startWith("f"), contains("la")
    // -> contains("fla")
    private void filter(String start, String contain) {
        for (SearchBean searchBean : mSearchList) {
            for (String tag : searchBean.tagList) {
                boolean find = false;
                String[] parts = tag.split("_");
                for (String part : parts) {
                    if (part.startsWith(start) && part.contains(contain)) {
                        if (!mPromptMap.containsKey(tag)) {
                            mPromptMap.put(tag, searchBean.colorId);
                        }
                        find = true;
                        break;
                    }
                }
                if (find) {
                    break;
                }
            }
            if (mPromptMap.size() >= 10) {
                break;
            }
        }
    }
}
