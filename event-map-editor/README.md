# NRO Event Map Editor

Editor chạy độc lập với server để bố trí effect sự kiện trực quan trên map.

## Chạy

Nhấp đúp `run-editor.bat`. Tool tự đọc data từ hai project nằm cạnh nó:

- `../nroteam-maven/data`
- Tile x4: `../nroteam-maven/data/res/x4/{tileId}${frame}`
- Effect x4: `../nroteam-maven/data/effect/x4`
- BG item x4: `../nroteam-maven/data/item_bg_temp/x4`
- Background x4: `../prj247/Assets/Resources/res/x4/bg`
- Database trong `../nroteam-maven/data/config/sever.properties`

## Sử dụng

1. Chọn map và sự kiện trên thanh công cụ.
2. Kéo asset từ bảng bên phải và thả lên map.
3. Kéo effect đã đặt để đổi vị trí; bảng thuộc tính cho phép nhập X/Y chính xác.
4. `Delete` để xóa, `Ctrl+D` để nhân bản, `Ctrl+S` để lưu vào `map_template.eff_event`.

Editor chỉ hiển thị effect của event đang chọn nhưng vẫn giữ nguyên effect của các event khác khi lưu.
