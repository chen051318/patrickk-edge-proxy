package cmddevice.bean;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * Bean基础类
 * 实现hashCode、equals、toString方法。
 * 作为实体对象的通用超类。
 * 
 * @author  patrickkk
 */
public abstract class BaseBean implements Cloneable, Serializable {
	private static final long serialVersionUID = -3707046914855595598L;

	/**
	 * @see Object#hashCode()
	 * @author  patrickkk
	 */
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	/**
	 * @see Object#equals(Object)
	 * @author  patrickkk
	 */
	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	/**
	 * @see Object#toString()
	 * @author  patrickkk
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * 浅层复制(如果属性为引用类型则只复制属性的引用值)当前对象
	 * @param <T>
	 * @return
	 * @author  patrickkk
	 */
	@SuppressWarnings("unchecked")
	public <T extends BaseBean> T shallowClone() {
		try {
			return (T)clone();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
