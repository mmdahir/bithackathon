package com.example.mustafdahir.bithackathon.interfaces;

/**
 * Created by abdul on 2/4/2018.
 */

/**
 * This interface must be implemented by activities that contain this
 * fragment to allow an interaction in this fragment to be communicated
 * to the activity and potentially other fragments contained in that
 * activity.
 * <p>
 * See the Android Training lesson <a href=
 * "http://developer.android.com/training/basics/fragments/communicating.html"
 * >Communicating with Other Fragments</a> for more information.
 */
public interface OnStartInteractionListener {
    void OnInteractionListener(String switchCase, String startOptions, String response, String username, String password );
}