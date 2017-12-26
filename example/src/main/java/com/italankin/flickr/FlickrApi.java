package com.italankin.flickr;

import com.italankin.flickr.http.InterestingnessResponse;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

interface FlickrApi {

    @GET("?method=flickr.interestingness.getList&format=json&nojsoncallback=1&extras=url_l%2Cviews%2Cowner_name")
    Observable<InterestingnessResponse> interestingness(
            @Query("api_key") String apiKey,
            @Query("page") int page,
            @Query("perpage") int perPage
    );

}
