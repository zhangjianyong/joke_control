package com.doumiao.joke.lang;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

public class HttpClientHelper {
	private static final AbstractHttpClient client = new DefaultHttpClient() {
		protected HttpParams createHttpParams() {
			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params,
					HTTP.DEF_CONTENT_CHARSET.name());
			HttpProtocolParams.setUseExpectContinue(params, true);
			HttpConnectionParams.setTcpNoDelay(params, true);
			HttpConnectionParams.setSocketBufferSize(params, 8192);
			HttpProtocolParams
					.setUserAgent(params,
							"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.1.5)");
			HttpConnectionParams.setConnectionTimeout(params, 1000);
			HttpConnectionParams.setSoTimeout(params, 5000);
			return params;
		}

		protected ClientConnectionManager createClientConnectionManager() {
			SchemeRegistry schreg = new SchemeRegistry();
			schreg.register(new Scheme("http", 80, PlainSocketFactory
					.getSocketFactory()));
			schreg.register(new Scheme("https", 443, SSLSocketFactory
					.getSocketFactory()));

			DnsResolver dnsResolver = new SystemDefaultDnsResolver();
			PoolingClientConnectionManager cm = new PoolingClientConnectionManager(
					schreg, dnsResolver);
			cm.setMaxTotal(1000000000);
			cm.setDefaultMaxPerRoute(500000000);
			return cm;
		}
	};

	public static HttpClient getClient() {
		return client;
	}

}
