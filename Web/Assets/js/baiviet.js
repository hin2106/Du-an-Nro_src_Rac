function formatTime(timestamp) {
    var currentTime = Math.floor(Date.now() / 1000);
    var difference = currentTime - timestamp;
    if (difference < 60) {
        return "Vừa xong"
    } else if (difference >= 60 && difference < 3600) {
        var minutes = Math.floor(difference / 60);
        return minutes + " phút trước"
    } else if (difference >= 3600 && difference < 86400) {
        var hours = Math.floor(difference / 3600);
        return hours + " giờ trước"
    } else {
        var days = Math.floor(difference / 86400);
        return days + " ngày trước"
    }
}

function cvhText(text, maxWidth) {
    const canvas = document.createElement('canvas');
    const context = canvas.getContext('2d');
    context.font = '16px Arial';
    let truncatedText = text;
    while (context.measureText(truncatedText).width > maxWidth) {
        truncatedText = truncatedText.slice(0, -1)
    }
    if (text.length > truncatedText.length) {
        truncatedText += '...'
    }
    return truncatedText
}
var currentPage = 1,
    postsPerPage = 9;

function loadPosts(a) {
    $.ajax({
        url: "/Api/Post/Load",
        method: "POST",
        data: {
            page: a,
            postsPerPage: postsPerPage
        },
        dataType: "json",
        success: function (t) {
            displayPosts(t.data), generatePagination(t.total_pages, a)
        },
        error: function (a, t, i) {
            console.log(i)
        },
    })
}

function displayPosts(a) {
    var t = "";
    $.each(a, function (a, i) {
        var s = '<div class="pb-3 pt-2 border-bottom">';
        s += '<a href="/bai-viet/' + i.id + '">';
        s += '<div class="d-flex align-items-center fs-3 col-12">';
        s += '<img src="/assets/images/avatar/' + i.head + '.png" width="35" alt="" />';
        s += '<div class="ms-2">';
        s += '<div class="user-meta-info">';
        s += '<h6 class="text-danger">' + cvhText(i.title, window.innerWidth * 0.8) + '</h6>';
        s += '<span class="fs-3 text-bodycolor d-flex align-items-center text-decoration-none"><i class="ti ti-thumb-up fs-5 text-info me-1 d-flex"></i> ' + i.likes + ' Lượt thích <i class="ti ti-clock fs-5 text-primary me-1 ms-2 d-flex"></i>' + formatTime(i.time) + '</span>';
        s += "</div>";
        s += "</div>";
        s += "</div>";
        s += "</div>";
        s += "</a>";
        s += "</div>";
        t += s
    }), $(".baiviet").html(t)
}

function generatePagination(a, t) {
    var i = "",
        s = Math.max(1, t - Math.floor(2.5)),
        e = Math.min(a, s + 5 - 1);
    s > 1 && (i += '<li class="page-item"><a class="page-link border-0 rounded-circle text-dark round-32 d-flex align-items-center justify-content-center" onclick="loadPosts(' + (t - 1) + ')"><i class="ti ti-chevron-left" aria-hidden="true"></i></a></li>');
    for (var n = s; n <= e; n++) i += n === t ? '<li class="page-item active" aria-current="page"><a class="page-link border-0 rounded-circle round-32 mx-1 d-flex align-items-center justify-content-center">' + n + "</a>" : '<li class="page-item"><a class="page-link border-0 rounded-circle text-dark round-32 mx-1 d-flex align-items-center justify-content-center" onclick="loadPosts(' + n + ')">' + n + "</a> </li>";
    e < a && (i += '<li class="page-item"><a class="page-link border-0 rounded-circle text-dark round-32 d-flex align-items-center justify-content-center" onclick="loadPosts(' + (t + 1) + ')"><i class="ti ti-chevron-right" aria-hidden="true"></i></a></li>'), $("#pagination").html(i)
}
loadPosts(currentPage);

function RandomString(arr) {
    var randomIndex = Math.floor(Math.random() * arr.length);
    return arr[randomIndex]
}
var $btns = $(".note-link").click(function () {
    if (this.id == "tat-ca") {
        var $el = $("." + this.id).fadeIn();
        $("#tat-ca-bai-viet > div").not($el).hide()
    }
    if (this.id == "important") {
        var $el = $("." + this.id).fadeIn();
        $("#tat-ca-bai-viet > div").not($el).hide()
    } else {
        var $el = $("." + this.id).fadeIn();
        $("#tat-ca-bai-viet > div").not($el).hide()
    }
    $btns.removeClass("active");
    $(this).addClass("active")
});
$("#them-bai-viet").on("click", function (event) {
    $("#modal-them-bai-viet").modal("show");
    $("#add").show()
});
$("#add").on("click", function (event) {
    event.preventDefault()
});
$("#addnotesmodal").on("hidden.bs.modal", function (event) {
    event.preventDefault();
    document.getElementById("title").value = "";
    document.getElementById("content").value = ""
});

$("#add").attr("disabled", "disabled");