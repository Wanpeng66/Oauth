package com.wp.oauth;

import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.ParameterStyle;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author: wp
 * @Title: ServerController
 * @Description: TODO
 * @date 2019/9/17 13:50
 */
@Controller
public class ServerController {

    @GetMapping("/returnCode")
    public String returnCode( Model model, HttpServletRequest request ){

        try {
            OAuthAuthzRequest authAuthzRequest = new OAuthAuthzRequest( request );
            if(!StringUtils.isEmpty( authAuthzRequest.getClientId() )){
                String authorizationCode ="authorizationCode";
                String response_type = authAuthzRequest.getParam( OAuth.OAUTH_RESPONSE_TYPE );
                OAuthASResponse.OAuthAuthorizationResponseBuilder builder =
                        OAuthASResponse.authorizationResponse(request, HttpServletResponse.SC_FOUND);
                builder.setCode( authorizationCode );
                final String redirect_url = authAuthzRequest.getParam( OAuth.OAUTH_REDIRECT_URI );
                OAuthResponse oAuthResponse = builder.location( redirect_url ).buildQueryMessage();
                String locationUri = oAuthResponse.getLocationUri();

                System.out.println(locationUri);

                HttpHeaders httpHeaders = new HttpHeaders(  );
                httpHeaders.setLocation( new URI( locationUri ) );

                return"redirect:"+locationUri;


            }


        } catch (OAuthSystemException e) {
            e.printStackTrace();
        } catch (OAuthProblemException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    @PostMapping("/returnToken")
    public HttpEntity returnToken( HttpServletRequest request, HttpServletResponse response){

        try {
            OAuthTokenRequest  authAuthzRequest = new OAuthTokenRequest( request );

            String secret = authAuthzRequest.getParam( OAuth.OAUTH_CLIENT_SECRET );
            String code = authAuthzRequest.getParam( OAuth.OAUTH_CODE );
            if(!StringUtils.isEmpty( secret )&&!StringUtils.isEmpty( code )){
                OAuthIssuerImpl impl = new OAuthIssuerImpl( new MD5Generator() );
                String accessToken = impl.accessToken();
                OAuthResponse oAuthResponse = OAuthASResponse.tokenResponse( HttpServletResponse.SC_OK )
                        .setAccessToken( accessToken ).buildJSONMessage();

                return new ResponseEntity( oAuthResponse.getBody(), HttpStatus.valueOf( oAuthResponse.getResponseStatus() ) );

            }
        } catch (OAuthSystemException e) {
            e.printStackTrace();
        } catch (OAuthProblemException e) {
            e.printStackTrace();
        }


        return null;
    }

    @GetMapping("/userInfoUrl")
    public HttpEntity  returnInfo(HttpServletRequest request,HttpServletResponse response){
        try {
            OAuthAccessResourceRequest resourceRequest = new OAuthAccessResourceRequest( request, ParameterStyle.QUERY );
            String accessToken = resourceRequest.getAccessToken();
            if(!StringUtils.isEmpty( accessToken )){
                JSONObject info = new JSONObject();
                info.put( "name","wp" );
                info.put( "age",24 );

                return new ResponseEntity( info.toString(),HttpStatus.OK );

            }


        } catch (OAuthSystemException e) {
            e.printStackTrace();
        } catch (OAuthProblemException e) {
            e.printStackTrace();
        }

        return null;
    }
}
