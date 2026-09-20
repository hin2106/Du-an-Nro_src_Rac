<div class="body-wrapper">
	<div class="container-fluid note-has-grid">
		<div class="row">
			<div class="col-lg-12">
				<div class="card bg-light-danger rounded-2">
					<div class="card-body text-center">
						<div class="d-flex align-items-center justify-content-center mb-4 pt-8">
							<a href="#">
								<img src="/Assets/images/profile/user-3.jpg"
									class="rounded-circle me-n2 card-hover border border-2 border-white" width="44"
									height="44">
							</a>
							<a href="#">
								<img src="/Assets/images/profile/user-2.jpg"
									class="rounded-circle me-n2 card-hover border border-2 border-white" width="44"
									height="44">
							</a>
							<a href="#">
								<img src="/Assets/images/profile/user-1.jpg"
									class="rounded-circle me-n2 card-hover border border-2 border-white" width="44"
									height="44">
							</a>
						</div>
						<h3 class="fw-semibold">Tham Gia Cộng Đồng</h3>
						<p class="fw-normal mb-4 fs-4">Cộng đồng thành viên đông đảo đã có ở Zalo</p>
						<a href="https://zalo.me/g/obcmeh506" class="btn btn-danger mb-8">Box Zalo [ Dame Gốc ]</a>
					</div>
				</div>
			</div>
		</div>
		<ul class="nav nav-pills p-3 mb-3 rounded align-items-center card flex-row">
			<li class="nav-item">
				<a href="/"
					class="nav-link note-link  d-flex align-items-center justify-content-center active px-3 px-md-3 me-0 me-md-2 text-body-color">
					<i class="ti ti-list fill-white me-0 me-md-1"></i> <span class="font-weight-medium">Tất
						Cả</span> </a>
			</li>
			<?php if ($user) { ?>
				<li class="nav-item ms-auto">
					<a href="javascript:void(0)" class="btn btn-danger d-flex align-items-center px-3" id="them-bai-viet">
						<i class="ti ti-plus me-0 me-md-1 fs-4"></i> <span class="font-weight-medium fs-3">Đăng Bài</span>
					</a>
				</li>
			<?php } ?>
		</ul>
		<div class="tab-content">
			<div class="card w-100">
				<div class="card-body">
					<?php
					$query = $CVH->query("SELECT * FROM `cvh_baiviet` WHERE `role` = 2 AND `if_admin` IS NOT NULL ORDER BY `time` DESC LIMIT 5");
					$i = 1;
					if (mysqli_num_rows($query) > 0) {
						while ($row = mysqli_fetch_assoc($query)) {
							$data = json_decode($row['if_admin'], true);
							?>
							<div class="note-has-grid row">
								<div class="pb-3 pt-2 border-bottom"><a href="/bai-viet/<?php echo $row['id']; ?>">
										<div class="d-flex align-items-center fs-3 col-12"><img
												src="<?php echo $data['avatar']; ?>" width="35" alt="">
											<div class="ms-2">
												<div class="user-meta-info">
													<b style="color: red;">
														<img src="https://cdn-icons-png.flaticon.com/128/616/616490.png"
															width="20">
														<?php echo $row['title']; ?>
													</b><span
														class="fs-3 text-bodycolor d-flex align-items-center text-decoration-none">
														<i class="ti ti-thumb-up fs-5 me-1 d-flex"></i>
														<?php echo $CVH->getTotalLikes($row['id']); ?> Lượt thích
														<i class="ti ti-clock fs-5 me-1 ms-2 d-flex"></i>
														<?php echo $CVH->time_ago($row['time']); ?>
													</span>
												</div>
											</div>
										</div>
									</a>
								</div>
							</div>
						<?php }
					} ?>
					<div id="tat-ca-bai-viet" class="note-has-grid row baiviet">
					</div>
				</div>
			</div>
			<nav aria-label="...">
				<ul class="pagination justify-content-center mb-0 mt-4" id="pagination">
				</ul>
			</nav>

		</div>
	</div>
</div>
<?php if ($user) { ?>
	<!-- Thêm Bài Viết -->
	<div class="modal fade" id="modal-them-bai-viet" tabindex="-1" role="dialog" aria-labelledby="addnotesmodalTitle"
		aria-hidden="true">
		<div class="modal-dialog modal-dialog-centered modal-lg" role="document">
			<div class="modal-content">
				<div class="modal-header">
					<h6 class="modal-title">Thêm Bài Viết</h6>
					<button type="button" class="btn-close btn-close-black" data-bs-dismiss="modal"
						aria-label="Close"></button>
				</div>
				<div class="modal-body">
					<div class="notes-box">
						<div class="notes-content">
							<form cvhvn="true" method="POST" action="/Api/Post/Add" href="<?php echo FULL_URL('/'); ?>">
								<div class="row">
									<div class="col-md-12 mb-3">
										<div class="note-title">
											<label>Tiêu Đề</label>
											<input type="text" name="title" class="form-control"
												placeholder="Nhập tiêu đề bài viết" />
										</div>
									</div>
									<div class="col-md-12">
										<div class="note-description">
											<label>Nội Dung</label>
											<textarea name="content" class="form-control editor" id="content-editor"
												placeholder="Nhập nội dung bài viết" rows="5"></textarea>
										</div>
									</div>
								</div>
								<div class="modal-footer">
									<a class="btn btn-danger" data-bs-dismiss="modal">Đóng</a>
									<button type="submit" href="<?php echo FULL_URL('/'); ?>" class="btn btn-primary">Thêm
										Ngay</button>
								</div>
							</form>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		$('#modal-them-bai-viet').on('shown.bs.modal', function () {
			if (!tinymce.get('content-editor')) {
				tinymce.init({
					selector: 'textarea#content-editor',
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
					image_class_list: [
						{ title: 'Responsive', value: 'img-fluid rounded-4 w-100 object-fit-cover' },
						{ title: 'Full Width', value: 'img-fluid w-100 object-fit-cover' }
					],
					importcss_append: true,
					file_picker_callback: function (callback, value, meta) {
						if (meta.filetype === 'image') {
							callback('', { alt: 'Image' });
						}
						if (meta.filetype === 'media') {
							callback('', { source2: '', poster: '' });
						}
					},
					height: 350,
					image_caption: true,
					quickbars_selection_toolbar: 'bold italic | quicklink h2 h3 blockquote quickimage quicktable',
					noneditable_noneditable_class: "mceNonEditable",
					toolbar_mode: 'sliding',
					contextmenu: "link image imagetools table",
					setup: function(editor) {
						editor.on('change', function() {
							editor.save();
						});
					}
				});
			}
		});
		
		// Sync TinyMCE content before form submit
		$('#modal-them-bai-viet form').on('submit', function(e) {
			if (tinymce.get('content-editor')) {
				tinymce.get('content-editor').save();
			}
		});
	</script>
<?php } ?>
</div>
</div>
<div class="dark-transparent sidebartoggler"></div>
<div class="dark-transparent sidebartoggler"></div>
</div>
</div>
<?php if ($setting['thongbao'] == 'true') { ?>
	<div class="modal fade" id="modalIndex" tabindex="-1" aria-modal="true" role="dialog">
		<div class="modal-dialog modal-dialog-centered">
			<div class="modal-content">
				<div class="modal-header d-flex align-items-center">
					<h4 class="modal-title">
						Thông Báo
					</h4>
					<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
				</div>
				<div class="modal-body">
					<?php echo $setting['nd_thongbao']; ?>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-light-danger text-danger font-medium waves-effect text-start"
						data-bs-dismiss="modal" onclick="hideNofication();">
						Đóng
					</button>
				</div>
			</div>
		</div>
	</div>
	<script>
		$(document).ready(function () {
			if (!getCookie('cvh_hidden')) {
				$('#modalIndex').modal('show');
			}
		});

		function hideNofication() {
			setCookie('cvh_hidden', true, 1);
		}
	</script>
<?php } ?>