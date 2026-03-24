\documentclass[12pt,a4paper]{article}

\usepackage[T5]{fontenc}
\usepackage[utf8]{inputenc}
\usepackage[vietnamese]{babel}
\usepackage[a4paper,margin=2.5cm]{geometry}
\usepackage{setspace}
\usepackage{parskip}
\usepackage{array}
\usepackage{longtable}
\usepackage{booktabs}

\setstretch{1.2}

\begin{document}

\begin{center}
{\LARGE \textbf{AI USAGE REPORT}}\\[0.2cm]
{\large Du an History Talk}
\end{center}

\section*{1. Nguyen tac su dung AI}
Bao cao nay chi ghi nhan cac noi dung co the doi chieu truc tiep tu source code. Cac phat bieu khong co bang chung trong repo deu duoc loai bo.

\section*{2. Cach nhom lam chu AI (Human-in-the-loop)}
Nhom ap dung quy trinh: AI goi y, con nguoi phe duyet.

\begin{itemize}
\item Buoc 1: Viet file markdown de lap ke hoach/review truoc khi thuc thi thay doi lon.
\item Buoc 2: Dung AI de de xuat huong trien khai va khung code.
\item Buoc 3: Con nguoi review lai output, sua thu cong theo dung convention va business rule.
\item Buoc 4: Build/test va doi chieu source truoc khi chap nhan ket qua.
\end{itemize}

Bang chung trong repo cho buoc review truoc khi code bao gom cac tai lieu markdown trong thu muc docs nhu:
\emph{authentication-plan.md, chat-messages-plan.md, document-processor-strategy-plan.md, design-pattern-review.md}.

\section*{3. Noi dung AI usage da xac minh tu source code}

\subsection*{3.1 Strategy + Factory cho Historical Context Document}
Code co \textbf{DocumentProcessorStrategy}, cac implementation va \textbf{DocumentProcessorFactory}. Trong service tao/cap nhat document, factory duoc goi de chon strategy theo \textit{DocumentType} roi moi process content.

\subsection*{3.2 Security theo role}
JWT filter doc claim \textit{uid, role}, tao authority dang \textit{ROLE\_...} va dat vao SecurityContext. Quyen STAFF/ADMIN cho cac API ghi duoc xu ly boi \textit{@PreAuthorize} o controller.

\subsection*{3.3 Flyway trong backend}
Project co dependency \textit{flyway-core} va co migration SQL trong resources. Tuy nhien, cau hinh hien tai dat \textit{spring.flyway.enabled=false}.

\subsection*{3.4 AI backend service va LLM provider}
Service Python build LLM voi 2 provider: Google (\textit{ChatGoogleGenerativeAI}) va OpenAI (\textit{ChatOpenAI}).

\section*{4. Bo sung AI usage theo cac US-Case lon (F01-F10)}

\subsection*{4.1 Nhom Customer (Nguoi hoc)}
\begin{longtable}{p{0.1\textwidth}p{0.34\textwidth}p{0.5\textwidth}}
\hline
{\bfseries US} & \textbf{Chuc nang} & \textbf{Cach su dung AI trong qua trinh lam chuc nang} \\
\midrule
F01 & Kham pha boi canh lich su & AI duoc dung de goi y cau truc API/doc response cho danh sach boi canh va tim kiem; nhom review lai logic search/pagination truoc khi merge. \\
F02 & Lua chon va xem ho so nhan vat & AI ho tro skeleton cho luong lay danh sach nhan vat va profile; nhom doi chieu lai mapper/entity de dam bao dung du lieu domain. \\
F03 & Tuong tac voi AI (chat) & AI ho tro de xuat request/response schema va xu ly message history; nhom kiem soat prompt, payload va ownership check trong backend. \\
F04 & Danh gia kien thuc (quiz) & AI duoc dung de de xuat flow tao/deploy quiz endpoint; nhom tu review rule cham diem, role check va ket qua tra ve. \\
F05 & Quan ly tai khoan & AI ho tro khung auth (request/response, validation); nhom kiem tra thu cong cac truong hop login/register/refresh theo security config. \\
F06 & Quan ly lich su hoc tap & AI goi y cach to chuc chat history va ket qua hoc tap; nhom doi chieu du lieu voi repository va gioi han truy cap theo user. \\
\bottomrule
\end{longtable}

\subsection*{4.2 Nhom Staff (Nhan vien/Quan tri vien)}
\begin{longtable}{p{0.1\textwidth}p{0.34\textwidth}p{0.5\textwidth}}
\hline
{\bfseries US} & \textbf{Chuc nang} & \textbf{Cach su dung AI trong qua trinh lam chuc nang} \\
\midrule
F07 & Quan ly noi dung lich su & AI de xuat huong tach xu ly noi dung theo Strategy + Factory; nhom chot implementation cuoi cung va bo sung validation theo business rule. \\
F08 & Quan ly nhan vat & AI ho tro sinh skeleton CRUD va DTO cho character; nhom sua thu cong de phu hop convention va quyen STAFF/ADMIN. \\
F09 & Quan ly ngan hang cau hoi & AI goi y cau truc endpoint va object model quiz; nhom review logic bo de, dap an va tinh toan ket qua. \\
F10 & Quan ly nguoi dung va nhan su & AI ho tro de xuat flow role/phan quyen; nhom doi chieu lai SecurityConfig, @PreAuthorize va kiem thu role thuc te. \\
\bottomrule
\end{longtable}

\section*{5. Noi dung khong dua vao bao cao vi khong verify duoc 100\%}
\begin{itemize}
\item Cac metric phan tram hieu suat (+45\%, +40\%, +30\%, ...).
\item Cac claim ve da fix infinite recursion neu source khong co dau vet ro rang.
\item Cac audit item khong trung implementation thuc te (vi du Character Strategy/Factory).
\end{itemize}

\section*{6. Ket luan}
AI duoc dung nhu technical co-pilot, nhung quyen quyet dinh va kiem soat chat luong van nam o con nguoi thong qua quy trinh review markdown truoc, review code sau, va build/test bat buoc.

\section*{Phu luc: Mau AI Audit Log (chi ghi muc verify duoc)}
\begin{longtable}{p{0.08\textwidth}p{0.2\textwidth}p{0.35\textwidth}p{0.29\textwidth}}
\hline
{\bfseries STT} & \textbf{Module} & \textbf{Noi dung AI ho tro} & \textbf{Kiem soat cua con nguoi} \\
\midrule
1 & Historical Document & Goi y Strategy/Factory cho xu ly content theo DocumentType & Review service flow, sua thu cong, build/test \\
2 & Security & Goi y mapping role JWT vao authority & Doi chieu lai @PreAuthorize tai controller \\
3 & AI Integration & Goi y request/response shape khi goi AI service & Kiem tra payload va xu ly loi trong service \\
\bottomrule
\end{longtable}

\end{document}

