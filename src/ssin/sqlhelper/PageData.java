package ssin.sqlhelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 翻页的数据
 * @author IcekingT420
 *
 */
public class PageData<T> {
	/**
	 * 符合条件的总记录数
	 */
	private long total = 0l;
	/**
	 * 本页包含的记录数
	 */
	private List<T> content = new ArrayList<T>();
	
	private PageParam pageParam;
	
	private boolean hasContent(){
		if(content==null || content.isEmpty())
			return false;
		return true;
	}
	
	/**
	 * 返回本页记录的数量
	 * @return
	 */
	public int getSize(){
		if(hasContent())
			return content.size();
		return 0;
	}
	
	public long getTotal() {
		return total;
	}
	public void setTotal(long total) {
		this.total = total;
	}
	public List<T> getContent() {
		return content;
	}
	public void setContent(List<T> content) {
		this.content = content;
	}

	public PageParam getPageParam() {
		return pageParam;
	}

	public void setPageParam(PageParam pageParam) {
		this.pageParam = pageParam;
	}
}
