<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<body>
<h2>Hello World!</h2>

    SpringMVC 文件上传
    <form action="/manage/product/upload.do" name="fileUpload1" method="post" enctype="multipart/form-data">
        <input type="file" name="upload_file"/>
        <input type="submit" value="上传文件">
    </form>

    富文本 文件上传
    <form action="/manage/product/rich_text_img_upload.do" name="fileUpload2" method="post" enctype="multipart/form-data">
        <input type="file" name="upload_file"/>
        <input type="submit" value="上传文件">
    </form>
</body>
</html>
