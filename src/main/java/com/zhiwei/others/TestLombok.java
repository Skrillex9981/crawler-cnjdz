//package com.zhiwei.others;
//
//import lombok.Cleanup;
//
//import java.io.*;
//
///**
// * @author 张乙凡
// * @version 1.0
// * @date 2021/8/6
// * @SneakyThrows：可以对受检异常进行捕捉并抛出，可以改写上述的main方法如下：
// */
//public class TestLombok {
//    public static void main(String[] args) throws IOException {
//        File file = new File("D:\\Society\\ZhiWeiData\\crawler-project\\cnjdz\\src\\main\\resources\\test.txt");
//        @Cleanup
//        InputStream inputStream = new FileInputStream(file);
//        int len;
//        byte[] bs = new byte[1024];
//        while ((len = inputStream.read(bs))!=-1){
//            System.out.println("conten:"+new String(bs, 0 ,len));
//        }
//
//    }
//}
