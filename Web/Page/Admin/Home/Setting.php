<div class="body-wrapper">
    <div class="container-fluid note-has-grid">
        <div class="row">
            <div class="col-sm-12">
                <div class="card">
                    <div class="card-body">
                        <h5>Chỉnh sửa thông tin website</h5>
                        <form class="form pt-3" cvhvn="true" method="POST" action="/Api/Admin/Edit"
                            href="<?php echo getCurrentURL(); ?>">
                            <div class="mb-3 row">
                                <label for="example-text-input" class="col-md-2 col-form-label">Tiêu đề</label>
                                <div class="col-md-10">
                                    <input class="form-control" type="text" value="<?php echo $setting['title']; ?>"
                                        name="title">
                                </div>
                            </div>
                            <div class="mb-3 row">
                                <label for="example-text-input" class="col-md-2 col-form-label">Favicon</label>
                                <div class="col-md-10">
                                    <input class="form-control" type="text" value="<?php echo $setting['favicon']; ?>"
                                        name="favicon">
                                </div>
                            </div>
                            <div class="mb-3 row">
                                <label for="example-text-input" class="col-md-2 col-form-label">Logo</label>
                                <div class="col-md-10">
                                    <input class="form-control" type="text" value="<?php echo $setting['logo']; ?>"
                                        name="logo">
                                </div>
                            </div>
                            <div class="mb-3 row">
                                <label for="example-text-input" class="col-md-2 col-form-label">Size Logo</label>
                                <div class="col-md-10">
                                    <input class="form-control" type="number" value="<?php echo $setting['size_logo']; ?>"
                                        name="size_logo">
                                </div>
                            </div>
                            <div class="mb-3 row">
                                <label for="example-text-input" class="col-md-2 col-form-label">Background</label>
                                <div class="col-md-10">
                                    <input class="form-control" type="text" value="<?php echo $setting['background']; ?>"
                                        name="background">
                                </div>
                            </div>
                            <div class="mb-3 row">
                                <label for="eff_load" class="col-md-2 col-form-label">Hiệu ứng tải trang</label>
                                <div class="col-md-10">
                                    <select class="form-select col-12" name="eff_load">
                                        <option
                                            value="<?php echo $setting['eff_load'] == 'true' ? 'true' : 'false'; ?>">
                                            <?php echo $setting['eff_load'] == 'true' ? 'Đang hiển thị' : 'Đang bị tắt'; ?>
                                        </option>
                                        <option value="true">Hiển Thị</option>
                                        <option value="false">Không Hiển Thị</option>
                                    </select>
                                </div>
                            </div>
                            <div class="mb-3 row">
                                <label for="example-text-input" class="col-md-2 col-form-label">Author</label>
                                <div class="col-md-10">
                                    <input class="form-control" type="text" value="<?php echo $setting['author']; ?>"
                                        name="author">
                                </div>
                            </div>
                            <div class="mb-3 row">
                                <label for="example-search-input" class="col-md-2 col-form-label">Description</label>
                                <div class="col-md-10">
                                    <textarea class="form-control" type="text"
                                        name="description"><?php echo $setting['description']; ?></textarea>
                                </div>
                            </div>
                            <div class="mb-3 row">
                                <label for="example-search-input" class="col-md-2 col-form-label">Keywords</label>
                                <div class="col-md-10">
                                    <textarea class="form-control" type="text"
                                        name="keywords"><?php echo $setting['keywords']; ?></textarea>
                                </div>
                            </div>
                            <div class="mb-3 row">
                                <label for="nhanqua" class="col-md-2 col-form-label">Nhận quà</label>
                                <div class="col-md-10">
                                    <select class="form-select col-12" name="nhanqua">
                                        <option value="<?php echo $setting['nhanqua'] == 'true' ? 'true' : 'false'; ?>">
                                            <?php echo $setting['nhanqua'] == 'true' ? 'Đang hiển thị' : 'Đang bị tắt'; ?>
                                        </option>
                                        <option value="true">Hiển Thị</option>
                                        <option value="false">Không Hiển Thị</option>
                                    </select>
                                </div>
                            </div>
                            <div class="mb-3 row">
                                <label for="vongquay" class="col-md-2 col-form-label">Vòng quay</label>
                                <div class="col-md-10">
                                    <select class="form-select col-12" name="vongquay">
                                        <option
                                            value="<?php echo $setting['vongquay'] == 'true' ? 'true' : 'false'; ?>">
                                            <?php echo $setting['vongquay'] == 'true' ? 'Đang hiển thị' : 'Đang bị tắt'; ?>
                                        </option>
                                        <option value="true">Hiển Thị</option>
                                        <option value="false">Không Hiển Thị</option>
                                    </select>
                                </div>
                            </div>
                            <div class="mb-3 row">
                                <label for="example-text-input" class="col-md-2 col-form-label">Giá mở thành
                                    viên</label>
                                <div class="col-md-10">
                                    <input class="form-control" type="text"
                                        value="<?php echo $setting['amount_mtv']; ?>" name="amount_mtv">
                                </div>
                            </div>
                            <div class="mb-3 row">
                                <label for="thongbao" class="col-md-2 col-form-label">Thông báo</label>
                                <div class="col-md-10">
                                    <select class="form-select col-12" name="thongbao">
                                        <option
                                            value="<?php echo $setting['thongbao'] == 'true' ? 'true' : 'false'; ?>">
                                            <?php echo $setting['thongbao'] == 'true' ? 'Đang hiển thị' : 'Đang bị tắt'; ?>
                                        </option>
                                        <option value="true">Hiển Thị</option>
                                        <option value="false">Không Hiển Thị</option>
                                    </select>
                                </div>
                            </div>
                            <div class="mb-3 row">
                                <label for="example-search-input" class="col-md-2 col-form-label">Nội dung Thông Báo</label>
                                <div class="col-md-10">
                                    <textarea class="form-control editor" type="text" name="nd_thongbao"
                                        id="thongbao"><?php echo $setting['nd_thongbao']; ?></textarea>
                                </div>
                            </div>
                            <div class="mb-3 d-flex justify-content-end">
                                <button type="submit" class="btn btn-success" href="<?php echo getCurrentURL(); ?>">Lưu
                                    Lại</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    tinymce.init({
        selector: 'textarea#thongbao',
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
            { title: 'None', value: '' },
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