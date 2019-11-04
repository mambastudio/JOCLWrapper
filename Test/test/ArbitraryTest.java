/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

/**
 *
 * @author user
 */
public class ArbitraryTest {
    public static void main(String... args)
    {
        int a = 12;
        int x = Integer.SIZE - Integer.numberOfLeadingZeros(a - 1);
        System.out.println(x);
    }
}
