/*
 * Copyright 2013 Chris Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.senab.photup;

import com.facebook.android.Facebook.ServiceListener;   
import com.facebook.android.FacebookError;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import uk.co.senab.photup.facebook.Session;

public class MainActivity extends Activity { //MainActivity 一个Android 程序的入口

    static final int REQUEST_FACEBOOK_LOGIN = 99;
    //作者定义的一个变量，用来检测是否登录facebook成功，主要用于startActivityForResult和OnActivityResult

    @Override 
    /*
    @Override是伪代码,表示重写(当然不写也可以)
    不过写上有如下好处: 可以当注释用,方便阅读；
    编译器可以给你验证@Override下面的方法名是否是你父类中所有的，如果没有则报错。
    例如，你如果没写@Override，而你下面的方法名又写错了，
    这时你的编译器是可以编译通过的，因为编译器以为这个方法是你的子类中自己增加的方法。
    */
    protected void onCreate(Bundle savedInstanceState) {. 
        super.onCreate(savedInstanceState);

        Session session = Session.restore(this);
        if (null == session) {
            launchLoginActivity();   //在打开程序的时候，如果检测session，发现用户没有登录facebook，那先调用登录方法
        } else {
            launchSelectionActivity(session);   //反之，选择当前session用户登录的facebook账号
        }
    }

    private void launchLoginActivity() {  //这个就是调用了另一个class：LoginActivity的方法launchLoginActivity()
        startActivityForResult(new Intent(this, LoginActivity.class), REQUEST_FACEBOOK_LOGIN);  
        //登录，就要启动facebook  此方法与之后的onActivityResult()呼应
    }

    private void launchSelectionActivity(final Session session) { //此方法主要是在查找到了已登录的session后进行一些Debug
        // Extend Access Token if we're not on a debug build 
        if (!Flags.DEBUG) {
            session.getFb()
                    .extendAccessTokenIfNeeded(getApplicationContext(), new ServiceListener() {
                        public void onFacebookError(FacebookError e) {
                            e.printStackTrace();
                        }

                        public void onError(Error e) {
                            e.printStackTrace();
                        }

                        public void onComplete(Bundle values) {
                            session.save(getApplicationContext());
                        }
                    });
        }

        startActivity(new Intent(this, PhotoSelectionActivity.class)); //Debug之后调用class：PhotoSelectionActivity
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
        switch (requestCode) {
            case REQUEST_FACEBOOK_LOGIN:
                Session session = Session.restore(this);
                if (resultCode == RESULT_OK && null != session) {
                    // Refresh Accounts
                    PhotupApplication.getApplication(getApplicationContext()) 
                            .getAccounts(null, true);

                    // Start Selection Activity
                    launchSelectionActivity(session);
                } else {
                    finish();
                }
                return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}
