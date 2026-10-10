package com.openbrows.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private EditText urlInput;
    private LinearLayout bottomBar;
    private FrameLayout webViewContainer;
    
    private SharedPreferences prefs;
    private String defaultHome = "https://www.google.com";
    private int textSize = 100;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("OpenBrowsPrefs", MODE_PRIVATE);
        loadPreferences();

        webViewContainer = findViewById(R.id.webViewContainer);
        bottomBar = findViewById(R.id.bottomBar);
        urlInput = findViewById(R.id.urlInput);

        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnForward = findViewById(R.id.btnForward);
        ImageButton btnTabs = findViewById(R.id.btnTabs);
        ImageButton btnMore = findViewById(R.id.btnMore);

        setupWebView();

        btnBack.setOnClickListener(v -> {
            if (webView.canGoBack()) webView.goBack();
        });

        btnForward.setOnClickListener(v -> {
            if (webView.canGoForward()) webView.goForward();
        });

        btnTabs.setOnClickListener(v -> Toast.makeText(this, "Tabs panel active", Toast.LENGTH_SHORT).show());
        btnMore.setOnClickListener(this::showPopupMenu);

        urlInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                loadUrl(urlInput.getText().toString().trim());
                hideKeyboard();
                return true;
            }
            return false;
        });

        loadUrl(defaultHome);
    }

    private void setupWebView() {
        webView = new WebView(this);
        webViewContainer.addView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setTextZoom(textSize);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                urlInput.setText(url);
            }
        });
        webView.setWebChromeClient(new WebChromeClient());
    }

    private void loadUrl(String query) {
        if (query.startsWith("http://") || query.startsWith("https://")) {
            webView.loadUrl(query);
        } else if (query.contains(".") && !query.contains(" ")) {
            webView.loadUrl("https://" + query);
        } else {
            webView.loadUrl("https://www.google.com/search?q=" + android.net.Uri.encode(query));
        }
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add("New Tab");
        popup.getMenu().add("History");
        popup.getMenu().add("Settings");
        
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Settings")) {
                showSettingsDialog();
            } else {
                Toast.makeText(this, item.getTitle() + " clicked", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        popup.show();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("OpenBrows Settings");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText homeInput = new EditText(this);
        homeInput.setHint("Default Home Page");
        homeInput.setText(defaultHome);
        layout.addView(homeInput);

        builder.setView(layout);
        builder.setPositiveButton("Save", (dialog, which) -> {
            defaultHome = homeInput.getText().toString();
            prefs.edit().putString("home", defaultHome).apply();
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void loadPreferences() {
        defaultHome = prefs.getString("home", "https://www.google.com");
        textSize = prefs.getInt("textSize", 100);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}
