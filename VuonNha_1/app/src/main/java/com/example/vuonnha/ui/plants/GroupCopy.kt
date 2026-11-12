package com.example.vuonnha.ui.plants

import com.example.vuonnha.data.SpeciesGroup

internal data class GroupCopy(
    val stepOne: String,
    val stepTwo: String,
    val stepOneDescription: String,
    val stepOneTitle: String,
    val speciesLabel: String,
    val speciesPlaceholder: String,
    val stepTwoTitle: String,
    val stepTwoDescription: String,
    val stepThreeTitle: String,
    val stepThreeDescription: String,
    val saveSummaryPrefix: String,
    val detailTitle: String,
    val stepThreeIntro: String
)

internal fun groupCopy(group: SpeciesGroup): GroupCopy = when (group) {
    SpeciesGroup.PLANT -> GroupCopy(
        "Chọn cây", "Góc trồng", "Bắt đầu với tên cây. Bạn có thể tra cứu hoặc tự nhập.",
        "Bạn đang trồng cây gì?", "Tên loài hoặc mô tả *", "VD: Cây kim tiền, cây lá tím chưa rõ tên",
        "Cây sẽ ở đâu?", "Đặt tên thân quen và chọn một góc cho cây. Các mục này đều tuỳ chọn.",
        "Lịch chăm cây", "Đặt lịch kiểm tra cây; chỉ tưới hoặc bón khi cây cần.",
        "Sẽ lưu hồ sơ cây", "Thông tin cây", "Chọn việc muốn được nhắc. Bạn có thể lưu cây mà chưa đặt lịch."
    )
    SpeciesGroup.FISH -> GroupCopy(
        "Chọn cá", "Bể của cá", "Bắt đầu với loài cá bạn nuôi.",
        "Bạn đang nuôi cá gì?", "Tên loài cá *", "VD: Cá betta, cá bảy màu",
        "Cá sẽ ở bể nào?", "Đặt tên cho bể hoặc nhóm cá và ghi thông tin bể. Các mục đều tuỳ chọn.",
        "Lịch chăm cá", "Chọn việc cần nhắc: cho ăn, thay nước, vệ sinh lọc.",
        "Sẽ lưu hồ sơ cá", "Thông tin bể cá", "Chọn việc muốn được nhắc. Bạn có thể lưu cá mà chưa đặt lịch."
    )
    SpeciesGroup.PET -> GroupCopy(
        "Chọn thú cưng", "Nơi ở", "Bắt đầu với loài hoặc giống bạn nuôi.",
        "Bạn đang nuôi thú cưng gì?", "Tên loài hoặc giống *", "VD: Hamster, mèo Anh lông ngắn",
        "Bạn ấy sẽ ở đâu?", "Đặt tên thân quen và ghi nơi ở. Các mục đều tuỳ chọn.",
        "Lịch chăm thú cưng", "Chọn việc cần nhắc: cho ăn, nước uống, dọn chuồng.",
        "Sẽ lưu hồ sơ thú cưng", "Thông tin thú cưng", "Chọn việc muốn được nhắc. Bạn có thể lưu thú cưng mà chưa đặt lịch."
    )
}
