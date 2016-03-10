package ssin.sqlhelper;

/**
 * 翻页的请求
 * @author IcekingT420
 *
 */
public class PageParam {
	private int page;
	private int rows;
	
	public PageParam() {
	}

	public PageParam(int page, int rows) {
		this.page = page;
		this.rows = rows;
	}
	
	public boolean isPageValid(){
		if(page>=0 && rows>=0)
			return true;
		return false;
	}
	
	public int getFirst(){
		return page*rows;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}
	
	/**
	 * easyUI的page从1开始，所以设置进来之后，需要-1
	 */
	public void pageSub1(){
		this.page--;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public int getSize() {
		return getRows();
	}
}
