<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div id="topBanner">
	<c:if test="${empty User}">
		<a href="${pageContext.servletContext.contextPath}/login">Log in</a>
	</c:if>
	<c:if test="${! empty User}">
		Logged in as ${User.username} | <a href="${pageContext.servletContext.contextPath}/logout">Log out</a>
	</c:if>
</div>