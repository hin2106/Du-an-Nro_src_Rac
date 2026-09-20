<?php
$result = mysqli_query($CVH->connect_db(), "SELECT * FROM `cvh_baiviet` WHERE id = '".$_GET['id']."' AND `role` = 2 AND `if_admin` IS NOT NULL ORDER BY `time`"); 
$row = mysqli_fetch_array($result, MYSQLI_ASSOC);
?>
<div class="body-wrapper">
    <div class="container-fluid note-has-grid">
        <div class="row">
            <div class="col-sm-12">
                <div class="card">
                    <div class="card-body">
                        <form class="form pt-3" cvhvn="true" method="POST" action="/Api/Admin/Post/Edit"
                            href="/admin/poster">
                            <input type="hidden" name="id" value="<?php echo $row['id']; ?>">
                            <div class="mb-3 row">
                                <label for="example-text-input" class="col-md-2 col-form-label">Tiêu đề</label>
                                <div class="col-md-10">
                                    <input class="form-control" type="text" name="title"
                                        placeholder="Nhập tiêu đề bài viết" value="<?php echo $row['title']; ?>">
                                </div>
                            </div>
                            <div class="mb-3 row">
                                <label for="avatar" class="col-md-2 col-form-label">Ảnh đại diện</label>
                                <div class="col-md-10">
                                    <select class="form-select col-12" name="avatar" id="avatarz">
                                        <?php
                                        $folderPath = 'Assets/images/avatar/admin';
                                        $files = scandir($folderPath);
                                        foreach ($files as $file) {
                                            if ($file != '.' && $file != '..' && is_file($folderPath . '/' . $file)) {
                                                $fileName = pathinfo($file, PATHINFO_FILENAME);
                                                echo '<option value="' . $fileName . '">Ảnh đại diện ' . $fileName . '</option>';
                                            }
                                        }
                                        ?>
                                    </select>

                                </div>
                            </div>
                            <div class="mb-3 row">
                                <label for="example-search-input" class="col-md-2 col-form-label">Nội dung</label>
                                <div class="col-md-10">
                                    <textarea class="form-control editor" type="text" name="content"
                                        id="content"><?php echo $row['content']; ?></textarea>
                                </div>
                            </div>
                            <div class="mb-3 d-flex justify-content-end">
                                <button type="submit" class="btn btn-success" href="/admin/poster">Lưu
                                    Lại</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</div>
<script type="text/javascript">
    tinymce.init({
        selector: 'textarea#content',
        plugins: 'preview importcss searchreplace autolink autosave save directionality code visualblocks visualchars fullscreen image link media template codesample table charmap pagebreak nonbreaking anchor insertdatetime advlist lists wordcount help charmap quickbars emoticons',
        imagetools_cors_hosts: ['picsum.photos'],
        menubar: 'file edit view insert format tools table help',
        toolbar: 'undo redo | bold italic underline strikethrough | fontfamily fontsize blocks | alignleft aligncenter alignright alignjustify | outdent indent |  numlist bullist | forecolor backcolor removeformat | pagebreak | charmap emoticons | fullscreen  preview save print | insertfile image media template link anchor codesample | ltr rtl',
        toolbar_sticky: true,
        autosave_ask_before_unload: true,
        autosave_interval: "30s",
        autosave_prefix: "{path}{query}-{id}-",
        autosave_restore_when_empty: false,
        autosave_retention: "2m",
        image_advtab: true,
        content_css: '//www.tiny.cloud/css/codepen.min.css',
        link_list: [
            { title: 'My page 1', value: 'http://www.tinymce.com' },
            { title: 'My page 2', value: 'http://www.moxiecode.com' }
        ],
        image_list: [
            { title: 'My page 1', value: 'http://www.tinymce.com' },
            { title: 'My page 2', value: 'http://www.moxiecode.com' }
        ],
        image_class_list: [
            { title: 'Cao Văn Huy', value: 'img-fluid rounded-4 w-100 object-fit-cover' },
            { title: 'Cao Văn Huy', value: 'img-fluid w-100 object-fit-cover' },
            { title: 'Some class', value: 'class-name' }
        ],
        importcss_append: true,
        file_picker_callback: function (callback, value, meta) {
            if (meta.filetype === 'file') {
                callback('https://www.google.com/logos/google.jpg', { text: 'My text' });
            }
            if (meta.filetype === 'image') {
                callback('https://www.google.com/logos/google.jpg', { alt: 'My alt text' });
            }
            if (meta.filetype === 'media') {
                callback('movie.mp4', { source2: 'alt.ogg', poster: 'https://www.google.com/logos/google.jpg' });
            }
        },
        templates: [
            { title: 'New Table', description: 'creates a new table', content: '<div class="mceTmpl"><table width="98%%"  border="0" cellspacing="0" cellpadding="0"><tr><th scope="col"> </th><th scope="col"> </th></tr><tr><td> </td><td> </td></tr></table></div>' },
            { title: 'Starting my story', description: 'A cure for writers block', content: 'Once upon a time...' },
            { title: 'New list with dates', description: 'New List with dates', content: '<div class="mceTmpl"><span class="cdate">cdate</span><br /><span class="mdate">mdate</span><h2>My List</h2><ul><li></li><li></li></ul></div>' }
        ],
        template_cdate_format: '[Date Created (CDATE): %m/%d/%Y : %H:%M:%S]',
        template_mdate_format: '[Date Modified (MDATE): %m/%d/%Y : %H:%M:%S]',
        height: 320,
        image_caption: true,
        quickbars_selection_toolbar: 'bold italic | quicklink h2 h3 blockquote quickimage quicktable',
        noneditable_noneditable_class: "mceNonEditable",
        toolbar_mode: 'sliding',
        contextmenu: "link image imagetools table",
    });
</script>