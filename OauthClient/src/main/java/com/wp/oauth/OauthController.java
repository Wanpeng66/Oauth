package com.wp.oauth;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: wp
 * @Title: OauthController
 * @Description: TODO
 * @date 2019/9/17 11:11
 */
@Controller
public class OauthController {

    @Value( "${clientId}" )
    String clientId ;
    @Value( "${clientSecret}" )
    String clientSecret ;
    @Value( "${accessTokenUrl01}" )
    String accessTokenUrl01 ;
    @Value( "${accessTokenUrl02}" )
    String accessTokenUrl02 ;
    @Value( "${userInfoUrl}" )
    String userInfoUrl ;
    @Value( "${redirectUrl01}" )
    String redirectUrl01 ;
    @Value( "${redirectUrl02}" )
    String redirectUrl02 ;
    @Value( "${response_type01}" )
    String response_type01 ;
    @Value( "${response_type02}" )
    String response_type02 ;


    @GetMapping("/")
    public String index(){
        return "index";
    }

    @GetMapping("/getCode")
    public String getCode( HttpServletRequest request, HttpServletResponse response, RedirectAttributes attr ){
        //1.第一步先构建一个OAuthClient对象，封装了一个http连接对象
        OAuthClient oAuthClient = new OAuthClient( new URLConnectionClient() );
        String requestUrl = null;
        try{
            //构建oauthd的请求。设置请求服务地址（accessTokenUrl）、clientId、response_type、redirectUrl
            OAuthClientRequest oAuthClientRequest = OAuthClientRequest.authorizationLocation( accessTokenUrl01  ).setResponseType( response_type01 )
                    .setClientId( clientId ).setRedirectURI( redirectUrl01 ).buildQueryMessage();
            requestUrl = oAuthClientRequest.getLocationUri();
            System.out.println(requestUrl);
        }catch(Exception e){
            e.printStackTrace();
        }

        return "redirect:"+requestUrl;
    }

    @GetMapping("/getCodeCallback")
    public String getCodeCallback( HttpServletRequest request, HttpServletResponse response, Model model ){
        String code = request.getParameter( "code" );
        OAuthClient oAuthClient = new OAuthClient( new URLConnectionClient() );
        try {
            OAuthClientRequest oAuthClientRequest = OAuthClientRequest.tokenLocation( accessTokenUrl02 )
                    .setClientId( clientId ).setClientSecret( clientSecret )
                    .setGrantType( GrantType.AUTHORIZATION_CODE ).setCode( code )
                    .setRedirectURI( redirectUrl02 ).buildQueryMessage();
            OAuthJSONAccessTokenResponse tokenResponse = oAuthClient.accessToken( oAuthClientRequest, OAuth.HttpMethod.POST );
            String accessToken = tokenResponse.getAccessToken();
            return "forward:getResource?token="+accessToken;

        } catch (OAuthSystemException e) {
            e.printStackTrace();
        } catch (OAuthProblemException e) {
            e.printStackTrace();
        }


        return "show";
    }

    @GetMapping("/getResource")
    public String getResouce(HttpServletRequest request,HttpServletResponse response,Model model){
        String token = request.getParameter( "token" );
        OAuthClient oAuthClient =new OAuthClient(new URLConnectionClient());

        try {
            OAuthClientRequest oAuthClientRequest = new OAuthBearerClientRequest( userInfoUrl ).setAccessToken( token ).buildQueryMessage();
            OAuthResourceResponse resource = oAuthClient.resource( oAuthClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class );
            String body = resource.getBody();
            model.addAttribute( "info",body );
            return "show";


        } catch (OAuthSystemException e) {
            e.printStackTrace();
        } catch (OAuthProblemException e) {
            e.printStackTrace();
        }

        return null;
    }




}
