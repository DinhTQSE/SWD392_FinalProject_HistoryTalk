# Design Pattern Review

## Giải thích cho newbie

### 1) Design pattern là gì?
Design pattern là "mẫu tổ chức code" đã được dùng nhiều lần và chứng minh là hiệu quả.

Mục tiêu chính:
- Code dễ đọc hơn cho người mới.
- Dễ mở rộng tính năng mới.
- Hạn chế sửa 1 chỗ làm hỏng chỗ khác.

### 2) Pattern đang dùng trong module document

#### Strategy Pattern
- Vai trò: định nghĩa nhiều cách xử lý khác nhau cho cùng một bài toán.
- Trong dự án này:
- `DocumentProcessorStrategy` là hợp đồng chung.
- `TextProcessorStrategy` xử lý nội dung text.
- `MarkdownProcessorStrategy` xử lý nội dung markdown.

Hiểu nhanh:
"Mỗi loại document có 1 cách xử lý riêng, nhưng đều tuân theo cùng một interface".

#### Factory Pattern
- Vai trò: chọn đúng strategy theo `DocumentType`.
- Trong dự án này: `DocumentProcessorFactory` nhận loại document rồi trả về strategy phù hợp.

Hiểu nhanh:
"Factory giống quầy phát tool: bạn đưa yêu cầu, factory đưa đúng công cụ".

#### Builder Pattern (Lombok)
- Vai trò: tạo object nhiều field theo cách rõ ràng và ít lỗi hơn.
- Trong dự án này: dùng `@Builder` khi tạo `HistoricalContextDocument`.

Hiểu nhanh:
"Builder giúp code tạo object dễ đọc, dễ bảo trì".

### 3) Luồng chạy thực tế (quan trọng nhất)
Khi gọi API create/update document, luồng thực tế đi qua các class sau:

#### Flow CREATE document
1. `HistoricalContextDocumentController.createDocument(...)`
	- Nhận `CreateHistoricalContextDocumentRequest`.
	- Lấy user hiện tại qua `SecurityUtils.getUserId()`.
	- Gọi `HistoricalContextDocumentService.createDocument(request, staffId)`.

2. `HistoricalContextDocumentServiceImpl.createDocument(...)`
	- Đọc context từ `HistoricalContextRepository.findById(...)`.
	- Đọc user từ `UserRepository.findById(...)`.
	- Xác định type: nếu request không truyền thì default `DocumentType.TEXT`.

3. `DocumentProcessorFactory.getStrategy(type)`
	- Factory map `DocumentType` -> strategy tương ứng (dựa trên `getSupportedType()`).
	- Trả về 1 trong các class:
	  - `TextProcessorStrategy`
	  - `MarkdownProcessorStrategy`

4. `DocumentProcessorStrategy.processContent(content)`
	- `TextProcessorStrategy.processContent(...)`: validate rỗng + giới hạn 10MB + trim.
	- `MarkdownProcessorStrategy.processContent(...)`: validate rỗng + giới hạn 10MB + sanitize script + trim.

5. Quay lại `HistoricalContextDocumentServiceImpl.createDocument(...)`
	- Dùng `HistoricalContextDocument.builder()` tạo entity.
	- Lưu DB qua `HistoricalContextDocumentRepository.save(doc)`.
	- Mapping response bằng `mapToResponse(...)` và trả về controller.

#### Flow UPDATE document
1. `HistoricalContextDocumentController.updateDocument(...)`
	- Nhận `docId` + `UpdateHistoricalContextDocumentRequest`.
	- Lấy user hiện tại bằng `SecurityUtils.getUserId()` và role bằng `SecurityUtils.getRoleName()`.
	- Gọi `HistoricalContextDocumentService.updateDocument(docId, request, staffId, staffRole)`.

2. `HistoricalContextDocumentServiceImpl.updateDocument(...)`
	- Tìm document qua `HistoricalContextDocumentRepository.findById(...)`.
	- Check quyền cập nhật với `isStaffOrAdmin(userRole)`.
	- Nếu đổi type thì set `doc.setDocumentType(...)`.

3. Nếu request có content mới
	- Gọi `DocumentProcessorFactory.getStrategy(doc.getDocumentType())`.
	- Gọi strategy `processContent(...)` để xử lý content theo type hiện tại.
	- Set lại `doc.setContent(processedContent)`.

4. Hoàn tất update
	- Lưu DB qua `HistoricalContextDocumentRepository.save(doc)`.
	- Mapping response bằng `mapToResponse(...)` rồi trả về controller.

Điểm mấu chốt:
- Service không cần biết chi tiết từng kiểu document xử lý ra sao.
- Service chỉ điều phối luồng, còn logic format nằm trong strategy.

### 4) Tại sao cách này tốt?
- Dễ thêm loại mới (VD: PDF, HTML) mà không cần sửa nhiều code cũ.
- Tránh if-else dài trong service.
- Giảm rủi ro regression khi mở rộng tính năng.

### 5) Ví dụ đời thường để nhớ nhanh
- Strategy = đầu bếp chuyên từng món.
- Factory = lễ tân nhận món và gọi đúng đầu bếp.
- Service = quản lý quy trình, không trực tiếp nấu ăn.

### 6) Nên làm tiếp để học chắc hơn
- Viết unit test riêng cho từng strategy:
- case nội dung rỗng/null
- case vượt giới hạn kích thước
- case sanitize nội dung nguy hiểm (ví dụ script)
- Thử thêm một strategy mới (ví dụ `HtmlProcessorStrategy`) để thấy rõ lợi ích của Factory + Strategy.

### 7) Giải thích hình trong slide (để thuyết trình)
Hình đang mô tả kiến trúc `Strategy + Factory` dưới dạng UML tổng quát.

Ý nghĩa từng khối trong hình:
- `HistoricalDocumentService` (trên cùng): lớp điều phối nghiệp vụ, không tự xử lý chi tiết content.
- `IDocumentProcessorStrategy` (interface): hợp đồng chung cho tất cả cách xử lý document.
- `HistoricalContextProcessor`, `CharacterBioProcessor`: các strategy cụ thể (mỗi class xử lý 1 loại document).
- `DocumentProcessorFactory`: nhận `DocumentType` và trả về strategy phù hợp.
- `DocumentType` (enum): định nghĩa các loại tài liệu để factory chọn đúng processor.

Ý nghĩa các mũi tên trong hình:
- `asks`: Service gọi sang Factory để hỏi "với type này thì dùng processor nào?".
- `returns`: Factory trả về object dạng `IDocumentProcessorStrategy`.
- `uses`: Service giữ tham chiếu strategy và gọi `processContent(...)`.
- Mũi tên nét đứt từ class cụ thể xuống interface: class đó implement interface strategy.

Map từ tên trong hình sang code hiện tại của dự án:
- `HistoricalDocumentService` trong hình ~ `HistoricalContextDocumentServiceImpl` trong code.
- `IDocumentProcessorStrategy` trong hình ~ `DocumentProcessorStrategy` trong code.
- `HistoricalContextProcessor` trong hình ~ `TextProcessorStrategy` trong code.
- `CharacterBioProcessor` trong hình ~ `MarkdownProcessorStrategy` trong code.
- `DocumentProcessorFactory` giữ nguyên tên trong code.
- `DocumentType` giữ nguyên enum trong code.

Thông điệp chính khi trình bày hình:
- Service chỉ điều phối, không chứa if-else dài theo từng loại document.
- Việc xử lý theo từng loại được tách vào strategy riêng.
- Thêm loại mới chỉ cần thêm strategy mới + cập nhật enum/factory, không phá code cũ.

### 8) Script thuyết trình mẫu (2-3 phút)
"Ở slide này, em trình bày kiến trúc xử lý document theo Strategy kết hợp Factory.

Nhìn từ trên xuống, `HistoricalDocumentService` là lớp nghiệp vụ chính. Lớp này không trực tiếp xử lý text hay markdown, mà chỉ điều phối luồng.

Khi có request, service sẽ gọi `DocumentProcessorFactory` theo đúng mũi tên `asks`, truyền vào `DocumentType`. Factory sẽ trả về một object theo kiểu `IDocumentProcessorStrategy`, tức là đúng bộ xử lý cho loại tài liệu đó, thể hiện ở mũi tên `returns`.

Sau khi nhận strategy, service gọi `processContent(...)` theo mũi tên `uses`. Điểm quan trọng là service chỉ biết interface chung, không phụ thuộc vào implementation cụ thể.

Ở tầng dưới, `HistoricalContextProcessor` và `CharacterBioProcessor` là các class implement strategy, mỗi class xử lý một loại document riêng. Điều này giúp tách biệt trách nhiệm và dễ mở rộng.

Trong code hiện tại của nhóm em, các tên tương ứng là: `HistoricalContextDocumentServiceImpl`, `DocumentProcessorStrategy`, `TextProcessorStrategy`, `MarkdownProcessorStrategy`, và `DocumentProcessorFactory`.

Lợi ích của cách làm này là: giảm if-else phức tạp, tăng khả năng mở rộng, và khi thêm loại document mới thì chỉ cần thêm strategy mới mà không phải sửa luồng cũ." 

Script rút gọn 30 giây:
"Factory chịu trách nhiệm chọn đúng strategy theo `DocumentType`, còn service chỉ điều phối và gọi `processContent`. Nhờ đó hệ thống mở rộng tốt, ít rủi ro regression, và code dễ bảo trì hơn khi thêm loại tài liệu mới." 

## File tham chiếu chính
- [DocumentProcessorStrategy](Source-code/SWD392_FinalProject_HistoryTalk/history-talk-backend/src/main/java/com/historyTalk/service/historicalContext/strategy/DocumentProcessorStrategy.java)
- [TextProcessorStrategy](Source-code/SWD392_FinalProject_HistoryTalk/history-talk-backend/src/main/java/com/historyTalk/service/historicalContext/strategy/TextProcessorStrategy.java)
- [MarkdownProcessorStrategy](Source-code/SWD392_FinalProject_HistoryTalk/history-talk-backend/src/main/java/com/historyTalk/service/historicalContext/strategy/MarkdownProcessorStrategy.java)
- [DocumentProcessorFactory](Source-code/SWD392_FinalProject_HistoryTalk/history-talk-backend/src/main/java/com/historyTalk/service/historicalContext/strategy/DocumentProcessorFactory.java)
- [HistoricalContextDocumentServiceImpl](Source-code/SWD392_FinalProject_HistoryTalk/history-talk-backend/src/main/java/com/historyTalk/service/historicalContext/HistoricalContextDocumentServiceImpl.java)
