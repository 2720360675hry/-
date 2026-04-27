package com.example.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.sys.common.Result;
import com.example.sys.entity.Book;
import com.example.sys.entity.Record;
import com.example.sys.entity.User;
import com.example.sys.service.IBookService;
import com.example.sys.service.IRecordService;
import com.example.sys.util.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author xumj
 * @since 2025-09-08
 */
@RestController
@CrossOrigin
@RequestMapping("/sys/book")
public class BookController {
    @Autowired
    private IBookService IBookService;

    @Autowired
    private IRecordService IRecordService;

    @RequestMapping("/all")
    public List<Book> allBook(){
        List<Book> allBook=IBookService.list();
        return allBook;
    }

    @GetMapping("/getsall")
    public Result<List<Book>> getsall(){
        List<Book> books=IBookService.list();
        return Result.success(books);
    }

    //查询所有的图书
    @GetMapping("/allBook")
    public Result getAllBook(){
        System.out.println("收到查询请求");
        List<Book> bookList = IBookService.list();
        return Result.success(bookList);
    }

    //添加图书信息
    @PostMapping("/addBook")
    public Result addBook(@RequestBody Book book){
        System.out.println("收到添加请求"+book);

        // ThreadLocal获取用户信息
        User user = ThreadLocalUtil.get();
        //查看是否为管理员权限
        if(user.getUserRole().equals("ADMIN"))
        {
            book.setBookName(book.getBookName());
            book.setBookIsbn(book.getBookIsbn());
            book.setBookPress(book.getBookPress());
            book.setBookAuthor(book.getBookAuthor());
            book.setBookPagination(book.getBookPagination());
            book.setBookPrice(book.getBookPrice());
            //设置图书的上架时间为当前时间
            //book.setBookUploadtime(LocalDate.now().toString());
            //其他数据不设置默认为空
            IBookService.save(book);
            return Result.success("添加用户书本成功!");
        }else{
            return Result.error("不是管理员权限，无法对图书进行操作!");
        }
    }

    //删除图书
    @DeleteMapping("/deleteBook")
    public Result removeBook(@RequestParam("bookId") Integer bookId){
        //获取用户
        boolean result = IBookService.removeById(bookId);
        if (result) {
            return Result.success("删除图书成功!");
        } else {
            return Result.error("删除失败,图书可能不存在或是出现错误!");
        }
    }

    //更新图书信息
    @PutMapping("/updateBook")
    public Result updateBookMsg(@RequestBody Book book){
        UpdateWrapper<Book> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("book_id", book.getBookId())
                .set("book_name", book.getBookName())
                .set("book_isbn", book.getBookIsbn())
                .set("book_press", book.getBookPress())
                .set("book_author", book.getBookAuthor())
                .set("book_pagination", book.getBookPagination())
                .set("book_price", book.getBookPrice())
                .set("book_uploadtime", book.getBookUploadtime())
                .set("book_status", book.getBookStatus())
                .set("book_borrower", book.getBookBorrower())
                .set("book_borrowtime", book.getBookBorrowtime())
                .set("book_returntime", book.getBookReturntime());
        boolean result = IBookService.update(updateWrapper);
        if (result) {
            return Result.success("修改图书成功!");
        } else {
            return Result.error("修改失败,图书可能不存在或是出现错误!");
        }
    }

    //分页查询功能
    // 分页查询功能（支持搜索）
    @GetMapping("/pagelist")
    public Result<IPage<Book>> getAllBooks(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "state", required = false) String state) {

        System.out.println("收到查询图书请求 - 页码:" + page + ", 大小:" + size + ", 关键词:" + keyword + ", 状态:" + state);

        try {
            if (page < 1 || size <= 0) {
                return Result.error("分页参数错误");
            }

            Page<Book> bookPage = new Page<>(page, size);
            LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();

            // 关键词搜索（书名、作者、出版社）
            if (keyword != null && !keyword.trim().isEmpty()) {
                queryWrapper.and(wrapper -> wrapper
                        .like(Book::getBookName, keyword)
                        .or()
                        .like(Book::getBookAuthor, keyword)
                        .or()
                        .like(Book::getBookPress, keyword)
                );
            }

            // 状态筛选
            if (state != null && !state.trim().isEmpty()) {
                queryWrapper.eq(Book::getBookStatus, state);
            }

            IPage<Book> books = IBookService.page(bookPage, queryWrapper);
            return Result.success(books);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询异常");
        }
    }

    //用户借阅图书功能
    @GetMapping("/borrowBook")
    public Result userBorrowBook(String bookId){
        User user = ThreadLocalUtil.get();
//    查询用户当前借了是否达到三本书，如果达到，禁止借阅
        QueryWrapper<Book> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("book_borrower",user.getUserName());
        List<Book> books = IBookService.list(queryWrapper);
        if(books.size()>=3){
            return Result.error("借阅图书失败，借阅图书数量已达上限!");
        }
        //定义时间格式
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        //获取当前时间 格式为 yyyy-MM-dd
        String borrowTime = LocalDate.now().format(dateFormatter);
        //设置还书时间为当前时间的30天之后
        String returnTime = LocalDate.now().plusDays(30).format(dateFormatter);
        //通过book_id查找需要借阅的图书
        UpdateWrapper<Book> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("book_id",bookId)
                .set("book_borrower",user.getUserName())
                .set("book_status","1")
                .set("book_borrowtime",borrowTime)
                .set("book_returntime",returnTime);

        boolean result = IBookService.update(updateWrapper); // 调用 update 方法
        if (result) {
            return Result.success("借阅图书成功");
        } else {
            return Result.error("借阅失败，可能图书已被其他人借阅或者是其他问题!");
        }
    }

    // 获取用户当前借书数据
    @GetMapping("/currentBorrowedBooks")
    public Result currentBorrowedBooks(){
        User user = ThreadLocalUtil.get();
        // 查询book表中借书人为本人的信息
        LambdaQueryWrapper<Book> bookWrapper = new LambdaQueryWrapper<>();
        bookWrapper.eq(Book::getBookBorrower,user.getUserName());
        List<Book> bookList = IBookService.list(bookWrapper);

        System.out.println("当前所借图书");
        return Result.success(bookList);
    }

    //用户归还图书功能
    @GetMapping("/returnBook")
    public Result returnBook(@RequestParam("bookId") Integer bookId){
        // 获取用户信息
        User loginUser = ThreadLocalUtil.get();

        // 通过book_id查找需要归还的图书
        Book bookMsg = IBookService.getById(bookId);
        String TheBorrowtime = bookMsg.getBookBorrowtime();

        // 获取当前时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String TheReturntime = LocalDate.now().format(formatter);
        UpdateWrapper<Book> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("book_borrower",loginUser.getUserName())
                .eq("book_status","1")
                .eq("book_id",bookId)
                .set("book_status","0")
                .set("book_borrower",null)
                .set("book_borrowtime",null)
                .set("book_returntime",null);
        boolean result = IBookService.update(updateWrapper); // 调用 update 方法

        // 图书状态修改之后，还需要新增一条借阅记录
        Record record = new Record();
        record.setRecordBookname(bookMsg.getBookName());
        record.setRecordBorrower(loginUser.getUserName());
        record.setRecordBookisbn(bookMsg.getBookIsbn());
        record.setRecordBorrowtime(TheBorrowtime);
        record.setRecordRemandtime(TheReturntime);
        boolean result1 = IRecordService.save(record); // 调用 save 方法
        if (result1) {
            System.out.println("Record updated successfully.");
            return Result.success("归还图书成功");
        } else {
            return Result.error("归还失败，可能没有借阅信息或者是其他问题!");
        }
    }

    // 按状态查询图书
    @GetMapping("/search")
    public Result<List<Book>> searchByState(@RequestParam(required = false) String state) {
        QueryWrapper<Book> queryWrapper = new QueryWrapper<>();
        // 如果前端传入了state参数，就按state条件查询
        if (state != null && !state.isEmpty()) {
            queryWrapper.eq("state", state);
        }
        List<Book> books = IBookService.list(queryWrapper);
        return Result.success(books);
    }

}
