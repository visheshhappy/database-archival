<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<div>
	<form action="/execute">
	<span>Enter the Table Name</span>
	<input type="text" name="tableName" />
	<br />
	<span>Enter the where clause :   where</span>
	<input type="text" name="whereClause" />
	<br />
	<span>Enter the batch size</span>
	<input type="text" name="batchSize" />
	<br />
	<select name="action">
		<option value="archive">Archive</option>
		<option value="delete">Delete</option>
		<option value="archieveAndDelete">Archieve and delete</option>
		<option value="count">Count</option>
	</select>
	<input type="submit" value="submit" />
	</form>
</div>
</body>
</html>