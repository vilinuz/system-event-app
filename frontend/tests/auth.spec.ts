import { test, expect } from '@playwright/test';

test.describe('Authentication Flow', () => {
  test('should redirect unauthenticated users to login', async ({ page }) => {
    await page.goto('/');
    await expect(page).toHaveURL(/.*login/);
  });

  test('should allow user to login with valid credentials', async ({ page }) => {
    await page.goto('/login');
    
    // Check if the form is rendered
    await expect(page.locator('h2')).toContainText('Welcome Back');
    
    // Fill credentials
    await page.fill('input[id="username"]', 'admin');
    await page.fill('input[id="password"]', 'admin');
    
    // Submit
    await page.click('button[type="submit"]');
    
    // Should redirect to dashboard
    await expect(page).not.toHaveURL(/.*login/);
    await expect(page.locator('.navbar-brand')).toBeVisible();
  });

  test('should show error with invalid credentials', async ({ page }) => {
    await page.goto('/login');
    
    // Fill invalid credentials
    await page.fill('input[id="username"]', 'admin');
    await page.fill('input[id="password"]', 'wrongpassword');
    
    // Submit
    await page.click('button[type="submit"]');
    
    // Check for error message
    await expect(page.locator('.error-alert')).toBeVisible();
    await expect(page.locator('.error-alert')).toContainText('Invalid username or password');
  });
});
