package org.bebrb.client;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.util.Callback;

import org.bebrb.client.Client.OnError;
import org.bebrb.client.Client.OnResponse;
import org.bebrb.client.utils.Resources;
import org.bebrb.data.BaseDataSet.CacheControl;
import org.bebrb.data.DataPage;
import org.bebrb.server.data.DataSourceImpl.SortAttribute;
import org.bebrb.server.net.Command;
import org.bebrb.server.net.CommandFactory;
import org.bebrb.server.net.CommandOpenDataSource;
import org.bebrb.server.net.CommandOpenReferenceView;

public class Cache {
	
	static private Logger log = org.bebrb.client.utils.Logger.getLogger(); 

	public static Client openDirect(Host currentHost, String sessionId, String id,
			Map<String, Object> params, int maxSizeDataPage,
			List<SortAttribute> sorting, 
			final Callback<CommandOpenDataSource.Response, Void> handler,
			final Callback<Exception,Void> errhandler) {
		
		String[] cid = id.split("\\.");

		Client query = new Client(currentHost.domain, currentHost.port,
				new OnResponse() {
					@Override
					public void replyСame(String message) throws Exception {
						// parse
						CommandOpenDataSource.Response response = CommandFactory
								.createGson()
								.fromJson(message,CommandOpenDataSource.Response.class);
						if (response.getStatus() != Command.OK) {
							log.log(Level.SEVERE,
									response.getMessage()+ " detail:"+ response.getTrace());
							throw new Exception(
									String.format(Resources.getBungles().getString("ex-OnServerError"),
											response.getMessageForUser()));
						} else
							log.log(Level.INFO,"response:" + message);
						handler.call(response);
					}
			
		}, new OnError() {
			@Override
			public void errorCame(Exception ex) {
				errhandler.call(ex);
			}
			
		});
		Command cmd;
		// reference book
		if(cid.length>1) {
			cmd = new CommandOpenReferenceView(sessionId,cid[0],cid[1],params);
			((CommandOpenReferenceView)cmd).setPageSize(maxSizeDataPage);
			((CommandOpenReferenceView)cmd).setSorting(sorting);
		} else {
			cmd = new CommandOpenDataSource(sessionId,id, params);
			((CommandOpenDataSource)cmd).setPageSize(maxSizeDataPage);
			((CommandOpenDataSource)cmd).setSorting(sorting);
		};
		query.send(cmd);
		return query;
	}

	public static Client openFromCache(Host currentHost, CacheControl cacheControl,
			Date actualDate, String sessionId, String id,
			Map<String, Object> params, int maxSizeDataPage,
			List<SortAttribute> sorting,
			final Callback<CommandOpenDataSource.Response, Void> handler,
			final Callback<Exception,Void> errhandler) {
		
		return null;
		// TODO Auto-generated method stub
	}

	public static Client requestRecords(Host currentHost,String sessionId, Cursor cursor, int i,
			final Callback<CommandOpenDataSource.Response, Void> handler,
			final Callback<Exception,Void> errhandler) {
		
		return null;
		// TODO Auto-generated method stub
		
	}

	
	public static class Cursor {
		private BigInteger cursorId;
		private List<DataPage> dataPages;
		private CacheControl cacheControl;
		private int recordCount;
		
		public Cursor(BigInteger id, CacheControl cc,
				List<DataPage> pages, int allCount) {
			cursorId = id;
			cacheControl = cc;
			dataPages = pages;
			recordCount = allCount;
		}
		
		public BigInteger getCursorId() {
			return cursorId;
		}
		public List<DataPage> getDataPages() {
			return dataPages;
		}

		public CacheControl getCacheControl() {
			return cacheControl;
		}

		public int getRecordCount() {
			return recordCount;
		}

	}

}
