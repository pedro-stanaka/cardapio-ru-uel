package br.uel.easymenu.service;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.inject.Inject;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import static br.uel.easymenu.service.NetworkEvent.NetworkErrorType;

import br.uel.easymenu.App;
import br.uel.easymenu.R;
import br.uel.easymenu.model.Meal;
import roboguice.inject.InjectResource;

public class NetworkService {

    @Inject
    private RequestQueue requestQueue;

    @InjectResource(R.string.ip)
    private String ip;

    @InjectResource(R.string.url_current_meal)
    private String currentMealUrl;

    @Inject
    private MealService mealService;

    @Inject
    private EventBus eventBus;

    public void persistCurrentMealsFromServer(final NetworkServiceListener listener) {
        String url = ip + currentMealUrl;

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                List<Meal> meals = mealService.deserializeMeal(response);
                mealService.replaceMealsFromCurrentWeek(meals);

                if (listener != null) {
                    listener.onSuccess();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: Show custom message from server too
                NetworkErrorType errorType = errorMessage(error);

                if(listener != null) {
                    listener.onError(errorType);
                }
                NetworkEvent event = new NetworkEvent(errorType);
                eventBus.post(event);
                Log.e(App.TAG, "Error: " + errorType+"");
            }
        });

        requestQueue.add(request);
    }


    private NetworkErrorType errorMessage(VolleyError error) {
        NetworkErrorType errorType;
        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
           errorType = NetworkErrorType.NO_CONNECTION;
        } else if (error instanceof AuthFailureError) {
            errorType = NetworkErrorType.AUTH_ERROR;
        } else if (error instanceof ServerError) {
            errorType = NetworkErrorType.SERVER_ERROR;
        } else if (error instanceof NetworkError) {
            errorType = NetworkErrorType.GENERIC_ERROR;
        } else if (error instanceof ParseError) {
            errorType = NetworkErrorType.PARSE_ERROR;
        } else {
            errorType = NetworkErrorType.UNKNOWN_ERROR;
        }
        return errorType;
    }

    public void persistCurrentMealsFromServer() {
        this.persistCurrentMealsFromServer(null);
    }

    public interface NetworkServiceListener {

        public abstract void onSuccess();

        public abstract void onError(NetworkErrorType error);
    }

}
